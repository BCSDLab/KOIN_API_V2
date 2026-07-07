package in.koreatech.koin.domain.community.article.model;

import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

import java.time.LocalDateTime;

import in.koreatech.koin.common.model.BaseEntity;
import in.koreatech.koin.domain.community.article.service.summary.ArticleSummaryFailureType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "article_ai_summary_logs")
@NoArgsConstructor(access = PROTECTED)
public class ArticleAiSummaryLog extends BaseEntity {

    private static final int MESSAGE_LIMIT = 500;

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Integer id;

    @Column(name = "summary_id")
    private Integer summaryId;

    @Column(name = "article_id")
    private Integer articleId;

    @Column(name = "board_id")
    private Integer boardId;

    @Enumerated(STRING)
    @Column(name = "event_type", nullable = false)
    private ArticleAiSummaryLogType eventType;

    @Enumerated(STRING)
    @Column(name = "status")
    private ArticleAiSummaryStatus status;

    @Enumerated(STRING)
    @Column(name = "failure_type")
    private ArticleSummaryFailureType failureType;

    @Column(name = "message", length = MESSAGE_LIMIT)
    private String message;

    @Column(name = "retry_count")
    private Integer retryCount;

    @Column(name = "next_attempt_at", columnDefinition = "TIMESTAMP")
    private LocalDateTime nextAttemptAt;

    @Column(name = "worker_id", length = 100)
    private String workerId;

    @Builder
    private ArticleAiSummaryLog(
        Integer summaryId,
        Integer articleId,
        Integer boardId,
        ArticleAiSummaryLogType eventType,
        ArticleAiSummaryStatus status,
        ArticleSummaryFailureType failureType,
        String message,
        Integer retryCount,
        LocalDateTime nextAttemptAt,
        String workerId
    ) {
        this.summaryId = summaryId;
        this.articleId = articleId;
        this.boardId = boardId;
        this.eventType = eventType;
        this.status = status;
        this.failureType = failureType;
        this.message = truncate(message);
        this.retryCount = retryCount;
        this.nextAttemptAt = nextAttemptAt;
        this.workerId = workerId;
    }

    public static ArticleAiSummaryLog of(
        ArticleAiSummary summary,
        ArticleAiSummaryLogType eventType,
        ArticleSummaryFailureType failureType,
        String message,
        String workerId
    ) {
        Article article = summary.getArticle();
        Board board = article == null ? null : article.getBoard();
        return ArticleAiSummaryLog.builder()
            .summaryId(summary.getId())
            .articleId(article == null ? null : article.getId())
            .boardId(board == null ? null : board.getId())
            .eventType(eventType)
            .status(summary.getStatus())
            .failureType(failureType)
            .message(message)
            .retryCount(summary.getRetryCount())
            .nextAttemptAt(summary.getNextAttemptAt())
            .workerId(workerId)
            .build();
    }

    private String truncate(String value) {
        if (value == null || value.length() <= MESSAGE_LIMIT) {
            return value;
        }
        return value.substring(0, MESSAGE_LIMIT);
    }
}
