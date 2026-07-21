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
        assertThat(content).contains("<p>📅 신청은 5월 20일까지 접수됩니다.</p>");
        assertThat(content).doesNotContain("1. 📅");
        assertThat(content).endsWith("<p>원문</p>");
    }

    @Test
    void 렌더링할_요약은_최대_세_줄까지만_사용한다() {
        String content = renderer.prependSummary(
            "<p>원문</p>",
            List.of(
                "📅 신청은 5월 20일까지 접수됩니다.",
                "🎯 재학생을 대상으로 모집합니다.",
                "📝 신청서를 제출해야 합니다.",
                "💰 장학금은 50만원입니다."
            )
        );

        assertThat(content).contains("📅 신청은 5월 20일까지 접수됩니다.");
        assertThat(content).contains("📝 신청서를 제출해야 합니다.");
        assertThat(content).doesNotContain("💰 장학금은 50만원입니다.");
    }

    @Test
    void 허용되지_않은_prefix나_긴_요약은_렌더링하지_않는다() {
        String content = renderer.prependSummary(
            "<p>원문</p>",
            List.of(
                "🚨 임의 이모지입니다.",
                "📅 " + "가".repeat(221)
            )
        );

        assertThat(content).isEqualTo("<p>원문</p>");
    }

    @Test
    void 요약_본문은_HTML_escape한다() {
        String content = renderer.prependSummary(
            "<p>원문</p>",
            List.of("📌 <script>alert(1)</script>")
        );

        assertThat(content).contains("📌 &lt;script&gt;alert(1)&lt;/script&gt;");
        assertThat(content).doesNotContain("<script>alert(1)</script>");
    }
}
