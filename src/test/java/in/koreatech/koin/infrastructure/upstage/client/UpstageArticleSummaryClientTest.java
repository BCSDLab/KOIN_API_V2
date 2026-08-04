package in.koreatech.koin.infrastructure.upstage.client;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import in.koreatech.koin.domain.community.article.service.summary.ArticleAiSummaryProperties;
import in.koreatech.koin.domain.community.article.service.summary.ArticleSummaryPrompt;

class UpstageArticleSummaryClientTest {

    @Test
    void 게시글_요약은_solar_pro4를_최소_추론으로_호출한다() {
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
            .containsEntry("reasoning_effort", "minimal")
            .containsEntry("max_tokens", 2_048)
            .containsKey("response_format");
    }
}
