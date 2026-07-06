package in.koreatech.koin.infrastructure.upstage.client;

import java.time.Duration;
import java.util.LinkedHashSet;
import java.util.Set;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.buffer.DataBufferLimitException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import in.koreatech.koin.domain.community.article.service.summary.ArticleAiSummaryProperties;
import in.koreatech.koin.domain.community.article.service.summary.ArticleDocumentParseClient;
import in.koreatech.koin.domain.community.article.service.summary.ArticleSummaryExternalApiException;
import in.koreatech.koin.domain.community.article.service.summary.DocumentParseRequest;

@Component
public class UpstageDocumentParseClient implements ArticleDocumentParseClient {

    private static final int MAX_PARSED_TEXT_LENGTH = 8_000;
    private static final int PARSE_RESPONSE_MEMORY_MULTIPLIER = 2;

    private final WebClient webClient;
    private final WebClient downloadClient;
    private final ObjectMapper objectMapper;
    private final UpstageProperties upstageProperties;
    private final ArticleAiSummaryProperties summaryProperties;
    private final Object documentParseRateLimitLock = new Object();
    private long nextDocumentParseAtMillis = 0L;

    public UpstageDocumentParseClient(
        ObjectMapper objectMapper,
        UpstageProperties upstageProperties,
        ArticleAiSummaryProperties summaryProperties
    ) {
        this.objectMapper = objectMapper;
        this.upstageProperties = upstageProperties;
        this.summaryProperties = summaryProperties;
        int maxDocumentBytes = summaryProperties.getBoundedMaxDocumentBytes();
        this.webClient = WebClient.builder()
            .baseUrl(upstageProperties.getApiBaseUrl())
            .exchangeStrategies(ExchangeStrategies.builder()
                .codecs(configurer -> configurer.defaultCodecs()
                    .maxInMemorySize(parseResponseMaxInMemorySize(maxDocumentBytes)))
                .build())
            .build();
        this.downloadClient = WebClient.builder()
            .exchangeStrategies(ExchangeStrategies.builder()
                .codecs(configurer -> configurer.defaultCodecs()
                    .maxInMemorySize(maxDocumentBytes))
                .build())
            .build();
    }

    @Override
    public String parse(DocumentParseRequest request) {
        if (!StringUtils.hasText(upstageProperties.getApiKey())) {
            throw new ArticleSummaryExternalApiException("Upstage API key가 설정되지 않았습니다.", false, null);
        }
        try {
            byte[] document = download(request.url());
            awaitDocumentParseSlot();
            String rawResponse = webClient.post()
                .uri("/document-digitization")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + upstageProperties.getApiKey())
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(multipartBody(request, document)))
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(summaryProperties.getDocumentParseRequestTimeoutSeconds()))
                .block();
            return truncate(extractText(rawResponse));
        } catch (ArticleSummaryExternalApiException e) {
            throw e;
        } catch (WebClientResponseException e) {
            throw toExternalApiException("문서 파싱", e);
        } catch (Exception e) {
            if (isDataBufferLimitExceeded(e)) {
                throw new ArticleSummaryExternalApiException("Upstage 문서 파싱 응답 크기가 허용 범위를 초과했습니다.", false, null, e);
            }
            Duration retryAfter = UpstageRetryAfterResolver.resolveTransientFailure(e);
            throw new ArticleSummaryExternalApiException(
                "Upstage 문서 파싱 처리 중 오류가 발생했습니다. cause=%s".formatted(errorSummary(e)),
                true,
                retryAfter,
                e
            );
        }
    }

    private byte[] download(String url) {
        if (!StringUtils.hasText(url) || !url.startsWith("https://")) {
            throw new ArticleSummaryExternalApiException("허용되지 않은 문서 URL입니다.", false, null);
        }
        byte[] document;
        try {
            document = downloadClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(byte[].class)
                .timeout(Duration.ofSeconds(summaryProperties.getDocumentDownloadTimeoutSeconds()))
                .block();
        } catch (WebClientResponseException e) {
            throw toExternalApiException("문서 다운로드", e);
        } catch (Exception e) {
            if (isDataBufferLimitExceeded(e)) {
                throw new ArticleSummaryExternalApiException("문서 크기가 허용 범위를 초과했습니다.", false, null, e);
            }
            Duration retryAfter = UpstageRetryAfterResolver.resolveTransientFailure(e);
            throw new ArticleSummaryExternalApiException(
                "문서 다운로드 중 오류가 발생했습니다. cause=%s".formatted(errorSummary(e)),
                true,
                retryAfter,
                e
            );
        }
        if (document == null || document.length == 0) {
            throw new ArticleSummaryExternalApiException("문서 다운로드 결과가 비어 있습니다.", false, null);
        }
        if (document.length > summaryProperties.getBoundedMaxDocumentBytes()) {
            throw new ArticleSummaryExternalApiException("문서 크기가 허용 범위를 초과했습니다.", false, null);
        }
        return document;
    }

    private org.springframework.util.MultiValueMap<String, org.springframework.http.HttpEntity<?>> multipartBody(
        DocumentParseRequest request,
        byte[] document
    ) {
        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("model", "document-parse");
        builder.part("ocr", summaryProperties.getDocumentParseOcrMode());
        builder.part("output_formats", "[\"markdown\"]");
        builder.part("document", new ByteArrayResource(document) {
            @Override
            public String getFilename() {
                return StringUtils.hasText(request.fileName()) ? request.fileName() : "document";
            }
        });
        return builder.build();
    }

    private void awaitDocumentParseSlot() {
        int minIntervalMillis = summaryProperties.getDocumentParseMinIntervalMillis();
        if (minIntervalMillis <= 0) {
            return;
        }
        synchronized (documentParseRateLimitLock) {
            long now = System.currentTimeMillis();
            long waitMillis = nextDocumentParseAtMillis - now;
            if (waitMillis > 0) {
                try {
                    Thread.sleep(waitMillis);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new ArticleSummaryExternalApiException("문서 파싱 요청 대기 중 인터럽트가 발생했습니다.", true, null, e);
                }
                now = System.currentTimeMillis();
            }
            nextDocumentParseAtMillis = now + minIntervalMillis;
        }
    }

    String extractText(String rawResponse) throws Exception {
        if (!StringUtils.hasText(rawResponse)) {
            return "";
        }
        JsonNode root = objectMapper.readTree(rawResponse);

        String contentMarkdown = textAt(root, "/content/markdown");
        if (StringUtils.hasText(contentMarkdown)) {
            return contentMarkdown.trim();
        }

        String contentText = textAt(root, "/content/text");
        if (StringUtils.hasText(contentText)) {
            return contentText.trim();
        }

        Set<String> texts = new LinkedHashSet<>();
        collectElementText(root.path("elements"), texts);
        collectElementText(root.path("pages"), texts);
        if (texts.isEmpty()) {
            collectText(root, texts);
        }
        return String.join("\n", texts).trim();
    }

    private String textAt(JsonNode root, String pointer) {
        JsonNode node = root.at(pointer);
        if (!node.isTextual()) {
            return "";
        }
        return node.asText().trim();
    }

    private void collectElementText(JsonNode node, Set<String> texts) {
        if (node == null || node.isNull() || node.isMissingNode()) {
            return;
        }
        if (node.isArray()) {
            node.forEach(child -> collectElementText(child, texts));
            return;
        }
        if (!node.isObject()) {
            return;
        }

        addPreferredText(node.path("content"), texts);
        addPreferredText(node, texts);
        collectElementText(node.path("elements"), texts);
        collectElementText(node.path("children"), texts);
    }

    private void addPreferredText(JsonNode node, Set<String> texts) {
        if (node == null || node.isNull() || node.isMissingNode()) {
            return;
        }
        String markdown = textAt(node, "/markdown");
        if (StringUtils.hasText(markdown)) {
            texts.add(markdown);
            return;
        }
        String text = textAt(node, "/text");
        if (StringUtils.hasText(text)) {
            texts.add(text);
        }
    }

    private void collectText(JsonNode node, Set<String> texts) {
        if (node == null || node.isNull()) {
            return;
        }
        if (node.isObject()) {
            node.fields().forEachRemaining(entry -> {
                String key = entry.getKey();
                JsonNode value = entry.getValue();
                if (("text".equals(key) || "markdown".equals(key)) && value.isTextual()) {
                    String text = value.asText().trim();
                    if (StringUtils.hasText(text)) {
                        texts.add(text);
                    }
                    return;
                }
                collectText(value, texts);
            });
            return;
        }
        if (node.isArray()) {
            node.forEach(child -> collectText(child, texts));
        }
    }

    private String truncate(String value) {
        if (value == null) {
            return "";
        }
        if (value.length() <= MAX_PARSED_TEXT_LENGTH) {
            return value;
        }
        return value.substring(0, MAX_PARSED_TEXT_LENGTH);
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
        return ", body=" + truncateForMessage(responseBody.replaceAll("\\s+", " ").trim(), 300);
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
        return truncateForMessage(summary, 300);
    }

    private Throwable rootCause(Throwable throwable) {
        Throwable root = throwable;
        while (root.getCause() != null && root.getCause() != root) {
            root = root.getCause();
        }
        return root;
    }

    private boolean isDataBufferLimitExceeded(Throwable throwable) {
        return rootCause(throwable) instanceof DataBufferLimitException;
    }

    private int parseResponseMaxInMemorySize(int maxDocumentBytes) {
        long maxResponseBytes = (long) maxDocumentBytes * PARSE_RESPONSE_MEMORY_MULTIPLIER;
        return (int) Math.min(maxResponseBytes, Integer.MAX_VALUE);
    }

    private String truncateForMessage(String value, int maxLength) {
        if (value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength - 3) + "...";
    }

}
