package in.koreatech.koin.unit.domain.community.article;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

import in.koreatech.koin.domain.community.article.dto.ArticleAiSummaryResponse;
import in.koreatech.koin.domain.community.article.service.summary.ArticleSummaryView;

class ArticleAiSummaryResponseTest {

    @Test
    void 저장된_요약_문장을_아이콘과_본문으로_분리한다() {
        ArticleSummaryView summaryView = ArticleSummaryView.success(List.of(
            "📅 신청은 5월 20일까지 접수됩니다.",
            "🎯 재학생을 대상으로 모집합니다."
        ));

        ArticleAiSummaryResponse response = ArticleAiSummaryResponse.from(summaryView);

        assertThat(response.status()).isEqualTo(ArticleAiSummaryResponse.Status.SUCCESS);
        assertThat(response.items()).containsExactly(
            new ArticleAiSummaryResponse.InnerArticleAiSummaryItemResponse(
                "📅",
                "신청은 5월 20일까지 접수됩니다."
            ),
            new ArticleAiSummaryResponse.InnerArticleAiSummaryItemResponse(
                "🎯",
                "재학생을 대상으로 모집합니다."
            )
        );
    }

    @Test
    void 표시할_수_없는_요약만_있으면_UNAVAILABLE을_반환한다() {
        ArticleSummaryView summaryView = ArticleSummaryView.success(List.of("아이콘이 없는 요약"));

        ArticleAiSummaryResponse response = ArticleAiSummaryResponse.from(summaryView);

        assertThat(response.status()).isEqualTo(ArticleAiSummaryResponse.Status.UNAVAILABLE);
        assertThat(response.items()).isEmpty();
    }
}
