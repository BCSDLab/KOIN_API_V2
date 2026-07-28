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
    void 게시글_요약은_solar_open2를_최소_추론으로_호출한다() {
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

        assertThat(summaryProperties.getModel()).isEqualTo("solar-open2");
        assertThat(requestBody)
            .containsEntry("model", "solar-open2")
            .containsEntry("reasoning_effort", "minimal")
            .containsKey("response_format");
    }
}
