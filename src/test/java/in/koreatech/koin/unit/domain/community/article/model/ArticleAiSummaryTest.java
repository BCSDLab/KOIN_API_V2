package in.koreatech.koin.unit.domain.community.article.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import in.koreatech.koin.domain.community.article.model.Article;
import in.koreatech.koin.domain.community.article.model.ArticleAiSummary;
import in.koreatech.koin.domain.community.article.model.ArticleAiSummaryStatus;

class ArticleAiSummaryTest {

    @Test
    void 요약_작업을_처리한_뒤_성공_상태와_요약문을_저장한다() {
        LocalDateTime sourceUpdatedAt = LocalDateTime.of(2026, 7, 17, 10, 0);
        LocalDateTime summarizedAt = LocalDateTime.of(2026, 7, 17, 10, 1);
        ArticleAiSummary summary = ArticleAiSummary.waiting(
            mock(Article.class),
            "fingerprint",
            sourceUpdatedAt,
            "solar-pro3",
            "v9"
        );

        summary.markProcessing("worker-1", summarizedAt.plusMinutes(30));
        summary.completeSuccess(
            List.of("첫 번째 요약입니다.", "두 번째 요약입니다."),
            "fingerprint",
            sourceUpdatedAt,
            "solar-pro3",
            "v9",
            summarizedAt
        );

        assertThat(summary.getStatus()).isEqualTo(ArticleAiSummaryStatus.SUCCESS);
        assertThat(summary.getSummaryLines()).containsExactly("첫 번째 요약입니다.", "두 번째 요약입니다.");
        assertThat(summary.isSuccessFor("fingerprint", "solar-pro3", "v9")).isTrue();
        assertThat(summary.getWorkerId()).isNull();
        assertThat(summary.getLockedUntil()).isNull();
    }

    @Test
    void 실패_사유는_DB_컬럼_길이에_맞춰_저장한다() {
        ArticleAiSummary summary = ArticleAiSummary.waiting(
            mock(Article.class),
            "fingerprint",
            LocalDateTime.now(),
            "solar-pro3",
            "v9"
        );

        summary.completeFailure("a".repeat(600), LocalDateTime.now().plusMinutes(1));

        assertThat(summary.getStatus()).isEqualTo(ArticleAiSummaryStatus.FAILED);
        assertThat(summary.getFailureReason()).hasSize(500);
        assertThat(summary.getRetryCount()).isEqualTo(1);
    }
}
