package in.koreatech.koin.infrastructure.upstage.client;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
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

    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final UpstageProperties upstageProperties;
    private final ArticleAiSummaryProperties summaryProperties;

    public UpstageArticleSummaryClient(
        ObjectMapper objectMapper,
        UpstageProperties upstageProperties,
        ArticleAiSummaryProperties summaryProperties
    ) {
        this.objectMapper = objectMapper;
        this.upstageProperties = upstageProperties;
        this.summaryProperties = summaryProperties;
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
            ChatCompletionResponse response = webClient.post()
                .uri("/chat/completions")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + upstageProperties.getApiKey())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody(prompt))
                .retrieve()
                .bodyToMono(ChatCompletionResponse.class)
                .timeout(Duration.ofSeconds(summaryProperties.getRequestTimeoutSeconds()))
                .block();

            String content = extractContent(response);
            SummaryJson summaryJson = objectMapper.readValue(stripCodeFence(content), SummaryJson.class);
            List<SummaryItemJson> items = summaryJson.items() == null ? List.of() : summaryJson.items();
            return new ArticleSummaryResult(items.stream()
                .map(item -> new ArticleSummaryItem(ArticleSummaryIcon.from(item.iconKey()), item.text()))
                .toList());
        } catch (ArticleSummaryExternalApiException e) {
            throw e;
        } catch (WebClientResponseException e) {
            throw toExternalApiException("요약", e);
        } catch (JsonProcessingException e) {
            throw new ArticleSummaryExternalApiException("Upstage 요약 응답 JSON 파싱에 실패했습니다.", true, null, e);
        } catch (Exception e) {
            throw new ArticleSummaryExternalApiException("Upstage 요약 처리 중 오류가 발생했습니다.", true, null, e);
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
            "max_tokens", 500,
            "reasoning_effort", "low",
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
                                        "maxLength", 260
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
        Duration retryAfter = retryable ? parseRetryAfter(e.getHeaders().getFirst(HttpHeaders.RETRY_AFTER)) : null;
        return new ArticleSummaryExternalApiException(
            "Upstage %s API 호출에 실패했습니다. status=%d".formatted(apiName, status),
            retryable,
            retryAfter,
            e
        );
    }

    private Duration parseRetryAfter(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        String trimmed = value.trim();
        try {
            return Duration.ofSeconds(Long.parseLong(trimmed));
        } catch (NumberFormatException ignored) {
            try {
                return Duration.between(
                    ZonedDateTime.now(),
                    ZonedDateTime.parse(trimmed, DateTimeFormatter.RFC_1123_DATE_TIME)
                );
            } catch (DateTimeParseException ignoredDateFormat) {
                return null;
            }
        }
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
