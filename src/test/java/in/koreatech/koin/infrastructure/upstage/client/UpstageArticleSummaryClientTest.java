package in.koreatech.koin.infrastructure.upstage.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import in.koreatech.koin.domain.community.article.service.summary.ArticleAiSummaryProperties;
import in.koreatech.koin.domain.community.article.service.summary.ArticleSummaryExternalApiException;
import in.koreatech.koin.domain.community.article.service.summary.ArticleSummaryPrompt;
import in.koreatech.koin.domain.community.article.service.summary.ArticleSummaryPromptBuilder;
import reactor.core.publisher.Mono;

class UpstageArticleSummaryClientTest {

    @Test
    void 게시글_요약은_solar_pro4를_호출한다() {
        ArticleAiSummaryProperties summaryProperties = new ArticleAiSummaryProperties();
        UpstageProperties upstageProperties = new UpstageProperties();
        upstageProperties.setApiKey("local-test-only");
        UpstageArticleSummaryClient client = new UpstageArticleSummaryClient(
            new ObjectMapper(),
            upstageProperties,
            summaryProperties
        );
        ArticleSummaryPrompt prompt = new ArticleSummaryPrompt(
            "system message",
            "user message",
            "source text",
            3
        );

        Map<String, Object> requestBody = ReflectionTestUtils.invokeMethod(client, "requestBody", prompt);

        assertThat(requestBody)
            .containsEntry("model", "solar-pro4")
            .containsEntry("reasoning_effort", "low")
            .containsKey("response_format")
            .doesNotContainKey("max_tokens");

        JsonNode schema = new ObjectMapper().valueToTree(requestBody)
            .at("/response_format/json_schema/schema/properties/items");
        assertThat(schema.path("maxItems").asInt()).isEqualTo(3);
        assertThat(schema.at("/items/properties/text/maxLength").asInt()).isEqualTo(100);

        ArticleSummaryPrompt fallback = new ArticleSummaryPromptBuilder().buildLegacyFallback(prompt);
        Map<String, Object> fallbackBody = ReflectionTestUtils.invokeMethod(client, "requestBody", fallback);
        JsonNode fallbackSchema = new ObjectMapper().valueToTree(fallbackBody)
            .at("/response_format/json_schema/schema/properties/items");
        assertThat(fallbackSchema.path("maxItems").asInt()).isEqualTo(3);
        assertThat(fallbackSchema.at("/items/properties/text/maxLength").asInt()).isEqualTo(200);

        AtomicReference<String> modelContent = new AtomicReference<>("{}");
        WebClient responseClient = WebClient.builder().exchangeFunction(request -> {
            String contentJson = new ObjectMapper().valueToTree(modelContent.get()).toString();
            return Mono.just(ClientResponse.create(HttpStatus.OK)
                .header("Content-Type", "application/json")
                .body("{\"choices\":[{\"message\":{\"content\":" + contentJson + "}}]}")
                .build());
        }).build();
        ReflectionTestUtils.setField(client, "webClient", responseClient);
        assertThatThrownBy(() -> client.summarize(prompt))
            .isInstanceOf(ArticleSummaryExternalApiException.class)
            .satisfies(exception -> assertThat(((ArticleSummaryExternalApiException)exception).isRetryable()).isTrue());
        modelContent.set("{\"items\":null}");
        assertThatThrownBy(() -> client.summarize(prompt))
            .isInstanceOf(ArticleSummaryExternalApiException.class)
            .hasMessageContaining("필수 items 배열");
        modelContent.set("{\"items\":[]}");
        assertThat(client.summarize(prompt).items()).isEmpty();
    }
}
