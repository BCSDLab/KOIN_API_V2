package in.koreatech.koin.infrastructure.upstage.client;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import in.koreatech.koin.domain.community.article.service.summary.ArticleAiSummaryProperties;
import in.koreatech.koin.domain.community.article.service.summary.ArticleSummaryExternalApiException;
import in.koreatech.koin.domain.community.article.service.summary.ArticleSummaryAiClient;
import in.koreatech.koin.domain.community.article.service.summary.ArticleSummaryIcon;
import in.koreatech.koin.domain.community.article.service.summary.ArticleSummaryItem;
import in.koreatech.koin.domain.community.article.service.summary.ArticleSummaryPrompt;
import in.koreatech.koin.domain.community.article.service.summary.ArticleSummaryResult;

@Component
public class UpstageArticleSummaryClient implements ArticleSummaryAiClient {

    private static final int MAX_OUTPUT_TOKENS = 2_048;

    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final UpstageProperties upstageProperties;
    private final ArticleAiSummaryProperties summaryProperties;
    private final UpstageChatTokenRateLimiter chatTokenRateLimiter;

    public UpstageArticleSummaryClient(
        ObjectMapper objectMapper,
        UpstageProperties upstageProperties,
        ArticleAiSummaryProperties summaryProperties
    ) {
        this.objectMapper = objectMapper;
        this.upstageProperties = upstageProperties;
        this.summaryProperties = summaryProperties;
        this.chatTokenRateLimiter = new UpstageChatTokenRateLimiter();
        this.webClient = WebClient.builder()
            .baseUrl(upstageProperties.getApiBaseUrl())
            .build();
    }

    @Override
    public ArticleSummaryResult summarize(ArticleSummaryPrompt prompt) {
        if (!StringUtils.hasText(upstageProperties.getApiKey())) {
            throw new ArticleSummaryExternalApiException("Upstage API key가 설정되지 않았습니다.", false, null);
        }
        try {
            chatTokenRateLimiter.await(prompt, MAX_OUTPUT_TOKENS);
            ResponseEntity<ChatCompletionResponse> responseEntity = webClient.post()
                .uri("/chat/completions")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + upstageProperties.getApiKey())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody(prompt))
                .retrieve()
                .toEntity(ChatCompletionResponse.class)
                .timeout(Duration.ofSeconds(summaryProperties.getChatRequestTimeoutSeconds()))
                .block();
            if (responseEntity == null) {
                throw new ArticleSummaryExternalApiException("Upstage 요약 응답이 비어 있습니다.", true, null);
            }
            chatTokenRateLimiter.update(responseEntity.getHeaders());

            String content = extractContent(responseEntity.getBody());
            SummaryJson summaryJson = objectMapper.readValue(stripCodeFence(content), SummaryJson.class);
            List<SummaryItemJson> items = summaryJson.items() == null ? List.of() : summaryJson.items();
            return new ArticleSummaryResult(items.stream()
                .map(item -> new ArticleSummaryItem(ArticleSummaryIcon.from(item.iconKey()), item.text()))
                .toList());
        } catch (ArticleSummaryExternalApiException e) {
            throw e;
        } catch (WebClientResponseException e) {
            chatTokenRateLimiter.update(e.getHeaders());
            throw toExternalApiException("요약", e);
        } catch (JsonProcessingException e) {
            throw new ArticleSummaryExternalApiException("Upstage 요약 응답 JSON 파싱에 실패했습니다.", true, null, e);
        } catch (Exception e) {
            Duration retryAfter = UpstageRetryAfterResolver.resolveTransientFailure(e);
            throw new ArticleSummaryExternalApiException(
                "Upstage 요약 처리 중 오류가 발생했습니다. cause=%s".formatted(errorSummary(e)),
                true,
                retryAfter,
                e
            );
        }
    }

    private Map<String, Object> requestBody(ArticleSummaryPrompt prompt) {
        return Map.of(
            "model", summaryProperties.getModel(),
            "messages", List.of(
                Map.of("role", "system", "content", prompt.systemMessage()),
                Map.of("role", "user", "content", prompt.userMessage())
            ),
            "temperature", 0.2,
            "top_p", 0.9,
            "max_tokens", MAX_OUTPUT_TOKENS,
            "reasoning_effort", "minimal",
            "response_format", responseFormat(prompt.maxItems())
        );
    }

    private Map<String, Object> responseFormat(int maxItems) {
        return Map.of(
            "type", "json_schema",
            "json_schema", Map.of(
                "name", "article_summary",
                "strict", true,
                "schema", Map.of(
                    "type", "object",
                    "additionalProperties", false,
                    "required", List.of("items"),
                    "properties", Map.of(
                        "items", Map.of(
                            "type", "array",
                            "minItems", 0,
                            "maxItems", maxItems,
                            "items", Map.of(
                                "type", "object",
                                "additionalProperties", false,
                                "required", List.of("icon_key", "text"),
                                "properties", Map.of(
                                    "icon_key", Map.of(
                                        "type", "string",
                                        "enum", List.of(
                                            "CALENDAR", "TARGET", "LOCATION", "ACTION",
                                            "MONEY", "NOTICE", "DOCUMENT", "DEFAULT"
                                        )
                                    ),
                                    "text", Map.of(
                                        "type", "string",
                                        "maxLength", 200
                                    )
                                )
                            )
                        )
                    )
                )
            )
        );
    }

    private String extractContent(ChatCompletionResponse response) {
        if (response == null || response.choices() == null || response.choices().isEmpty()) {
            throw new ArticleSummaryExternalApiException("Upstage 요약 응답이 비어 있습니다.", true, null);
        }
        String content = response.choices().get(0).message().content();
        if (!StringUtils.hasText(content)) {
            throw new ArticleSummaryExternalApiException("Upstage 요약 본문이 비어 있습니다.", true, null);
        }
        return content;
    }

    private ArticleSummaryExternalApiException toExternalApiException(String apiName, WebClientResponseException e) {
        int status = e.getStatusCode().value();
        boolean retryable = status == 429 || e.getStatusCode().is5xxServerError();
        Duration retryAfter = retryable
            ? UpstageRetryAfterResolver.resolve(status, e.getHeaders().getFirst(HttpHeaders.RETRY_AFTER))
            : null;
        return new ArticleSummaryExternalApiException(
            "Upstage %s API 호출에 실패했습니다. status=%d%s".formatted(apiName, status, responseBodySummary(e)),
            retryable,
            retryAfter,
            e
        );
    }

    private String responseBodySummary(WebClientResponseException e) {
        String responseBody = e.getResponseBodyAsString();
        if (!StringUtils.hasText(responseBody)) {
            return "";
        }
        return ", body=" + truncate(responseBody.replaceAll("\\s+", " ").trim(), 300);
    }

    private String errorSummary(Throwable throwable) {
        Throwable rootCause = rootCause(throwable);
        String message = rootCause.getMessage();
        if (!StringUtils.hasText(message) && rootCause != throwable) {
            message = throwable.getMessage();
        }
        String summary = rootCause.getClass().getSimpleName();
        if (StringUtils.hasText(message)) {
            summary += ": " + message.replaceAll("\\s+", " ").trim();
        }
        return truncate(summary, 300);
    }

    private Throwable rootCause(Throwable throwable) {
        Throwable root = throwable;
        while (root.getCause() != null && root.getCause() != root) {
            root = root.getCause();
        }
        return root;
    }

    private String truncate(String value, int maxLength) {
        if (value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength - 3) + "...";
    }

    private String stripCodeFence(String content) {
        String trimmed = content.trim();
        if (!trimmed.startsWith("```")) {
            return trimmed;
        }
        return trimmed
            .replaceFirst("^```(?:json)?\\s*", "")
            .replaceFirst("\\s*```$", "")
            .trim();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record ChatCompletionResponse(
        List<Choice> choices
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record Choice(
        Message message
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record Message(
        String content
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record SummaryJson(
        List<SummaryItemJson> items
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record SummaryItemJson(
        @JsonProperty("icon_key") String iconKey,
        String text
    ) {
    }
}
