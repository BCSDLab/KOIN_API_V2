package in.koreatech.koin.infrastructure.upstage.client;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import in.koreatech.koin.domain.community.article.service.summary.ArticleAiSummaryProperties;
import in.koreatech.koin.domain.community.article.service.summary.ArticleSummaryPrompt;

class UpstageArticleSummaryClientTest {

    @Test
    void 게시글_요약은_solar_pro4를_호출한다() {
        ArticleAiSummaryProperties summaryProperties = new ArticleAiSummaryProperties();
        UpstageArticleSummaryClient client = new UpstageArticleSummaryClient(
            new ObjectMapper(),
            new UpstageProperties(),
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
    }
}
