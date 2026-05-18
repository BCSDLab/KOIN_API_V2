package in.koreatech.koin.unit.domain.community.article.service.summary;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

import in.koreatech.koin.domain.community.article.service.summary.ArticleSummaryContentRenderer;

class ArticleSummaryContentRendererTest {

    private final ArticleSummaryContentRenderer renderer = new ArticleSummaryContentRenderer();

    @Test
    void 요약_블록을_content_맨_앞에_붙인다() {
        String content = renderer.prependSummary(
            "<p>원문</p>",
            List.of("📅 신청은 5월 20일까지 접수됩니다.", "🎯 재학생을 대상으로 모집합니다.")
        );

        assertThat(content).startsWith("<div class=\"ai-summary\">");
        assertThat(content).contains("✨ AI 요약");
        assertThat(content).contains("1. 📅 신청은 5월 20일까지 접수됩니다.");
        assertThat(content).endsWith("<p>원문</p>");
    }
}
