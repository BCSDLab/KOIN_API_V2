package in.koreatech.koin.infrastructure.upstage.client;

import java.time.Duration;
import java.util.LinkedHashSet;
import java.util.Set;

import org.springframework.core.io.ByteArrayResource;
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
import in.koreatech.koin.domain.community.article.service.summary.DocumentParseRequest;
import in.koreatech.koin.global.exception.custom.KoinIllegalStateException;

@Component
public class UpstageDocumentParseClient implements ArticleDocumentParseClient {

    private static final int MAX_PARSED_TEXT_LENGTH = 8_000;

    private final WebClient webClient;
    private final WebClient downloadClient;
    private final ObjectMapper objectMapper;
    private final UpstageProperties upstageProperties;
    private final ArticleAiSummaryProperties summaryProperties;

    public UpstageDocumentParseClient(
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
        this.downloadClient = WebClient.builder()
            .exchangeStrategies(ExchangeStrategies.builder()
                .codecs(configurer -> configurer.defaultCodecs()
                    .maxInMemorySize(summaryProperties.getMaxDocumentBytes()))
                .build())
            .build();
    }

    @Override
    public String parse(DocumentParseRequest request) {
        if (!StringUtils.hasText(upstageProperties.getApiKey())) {
            throw new KoinIllegalStateException("Upstage API key가 설정되지 않았습니다.");
        }
        try {
            byte[] document = download(request.url());
            String rawResponse = webClient.post()
                .uri("/document-digitization")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + upstageProperties.getApiKey())
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(multipartBody(request, document)))
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(summaryProperties.getRequestTimeoutSeconds()))
                .block();
            return truncate(extractText(rawResponse));
        } catch (WebClientResponseException e) {
            throw new KoinIllegalStateException("Upstage 문서 파싱 API 호출에 실패했습니다. status=" + e.getStatusCode());
        } catch (Exception e) {
            throw new KoinIllegalStateException("Upstage 문서 파싱 처리 중 오류가 발생했습니다.");
        }
    }

    private byte[] download(String url) {
        if (!StringUtils.hasText(url) || !url.startsWith("https://")) {
            throw new KoinIllegalStateException("허용되지 않은 문서 URL입니다.");
        }
        byte[] document = downloadClient.get()
            .uri(url)
            .retrieve()
            .bodyToMono(byte[].class)
            .timeout(Duration.ofSeconds(summaryProperties.getRequestTimeoutSeconds()))
            .block();
        if (document == null || document.length == 0) {
            throw new KoinIllegalStateException("문서 다운로드 결과가 비어 있습니다.");
        }
        if (document.length > summaryProperties.getMaxDocumentBytes()) {
            throw new KoinIllegalStateException("문서 크기가 허용 범위를 초과했습니다.");
        }
        return document;
    }

    private org.springframework.util.MultiValueMap<String, org.springframework.http.HttpEntity<?>> multipartBody(
        DocumentParseRequest request,
        byte[] document
    ) {
        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("model", "document-parse");
        builder.part("ocr", "force");
        builder.part("output_formats", "[\"text\", \"markdown\"]");
        builder.part("document", new ByteArrayResource(document) {
            @Override
            public String getFilename() {
                return StringUtils.hasText(request.fileName()) ? request.fileName() : "document";
            }
        });
        return builder.build();
    }

    private String extractText(String rawResponse) throws Exception {
        JsonNode root = objectMapper.readTree(rawResponse);
        Set<String> texts = new LinkedHashSet<>();
        collectText(root, texts);
        return String.join("\n", texts).trim();
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
        if (value.length() <= MAX_PARSED_TEXT_LENGTH) {
            return value;
        }
        return value.substring(0, MAX_PARSED_TEXT_LENGTH);
    }
}
