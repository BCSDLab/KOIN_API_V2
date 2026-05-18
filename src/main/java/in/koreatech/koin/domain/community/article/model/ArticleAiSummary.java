package in.koreatech.koin.domain.community.article.model;

import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import org.hibernate.annotations.Where;

import in.koreatech.koin.common.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Where(clause = "is_deleted=0")
@Table(name = "article_ai_summaries", uniqueConstraints = {
    @UniqueConstraint(name = "uk_article_ai_summaries_article_id", columnNames = "article_id")
})
@NoArgsConstructor(access = PROTECTED)
public class ArticleAiSummary extends BaseEntity {

    private static final int FAILURE_REASON_LIMIT = 500;

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Integer id;

    @OneToOne(fetch = LAZY)
    @JoinColumn(name = "article_id", nullable = false)
    private Article article;

    @NotNull
    @Enumerated(STRING)
    @Column(name = "status", nullable = false)
    private ArticleAiSummaryStatus status = ArticleAiSummaryStatus.WAIT;

    @Column(name = "summary_content", columnDefinition = "MEDIUMTEXT")
    private String summaryContent;

    @Column(name = "summarized_at", columnDefinition = "TIMESTAMP")
    private LocalDateTime summarizedAt;

    @Column(name = "source_fingerprint", length = 64)
    private String sourceFingerprint;

    @Column(name = "source_updated_at", columnDefinition = "TIMESTAMP")
    private LocalDateTime sourceUpdatedAt;

    @Column(name = "model", length = 100)
    private String model;

    @Column(name = "prompt_version", length = 20)
    private String promptVersion;

    @NotNull
    @Column(name = "retry_count", nullable = false)
    private Integer retryCount = 0;

    @Column(name = "next_attempt_at", columnDefinition = "TIMESTAMP")
    private LocalDateTime nextAttemptAt;

    @Column(name = "locked_until", columnDefinition = "TIMESTAMP")
    private LocalDateTime lockedUntil;

    @Column(name = "worker_id", length = 100)
    private String workerId;

    @Column(name = "failure_reason", length = 500)
    private String failureReason;

    @NotNull
    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    @Builder
    private ArticleAiSummary(
        Article article,
        ArticleAiSummaryStatus status,
        String sourceFingerprint,
        LocalDateTime sourceUpdatedAt,
        String model,
        String promptVersion
    ) {
        this.article = article;
        this.status = status;
        this.sourceFingerprint = sourceFingerprint;
        this.sourceUpdatedAt = sourceUpdatedAt;
        this.model = model;
        this.promptVersion = promptVersion;
    }

    public static ArticleAiSummary waiting(
        Article article,
        String sourceFingerprint,
        LocalDateTime sourceUpdatedAt,
        String model,
        String promptVersion
    ) {
        return ArticleAiSummary.builder()
            .article(article)
            .status(ArticleAiSummaryStatus.WAIT)
            .sourceFingerprint(sourceFingerprint)
            .sourceUpdatedAt(sourceUpdatedAt)
            .model(model)
            .promptVersion(promptVersion)
            .build();
    }

    public boolean isSuccessFor(String fingerprint, String model, String promptVersion) {
        return status == ArticleAiSummaryStatus.SUCCESS
            && Objects.equals(sourceFingerprint, fingerprint)
            && Objects.equals(this.model, model)
            && Objects.equals(this.promptVersion, promptVersion)
            && summaryContent != null
            && !summaryContent.isBlank();
    }

    public boolean isProcessing() {
        return status == ArticleAiSummaryStatus.PROCESSING;
    }

    public boolean isProcessingBy(String workerId) {
        return isProcessing() && Objects.equals(this.workerId, workerId);
    }

    public List<String> getSummaryLines() {
        if (summaryContent == null || summaryContent.isBlank()) {
            return List.of();
        }
        return summaryContent.lines()
            .map(String::trim)
            .filter(line -> !line.isBlank())
            .toList();
    }

    public void prepareWait(
        String sourceFingerprint,
        LocalDateTime sourceUpdatedAt,
        String model,
        String promptVersion
    ) {
        if (status == ArticleAiSummaryStatus.PROCESSING) {
            return;
        }
        this.status = ArticleAiSummaryStatus.WAIT;
        this.summaryContent = null;
        this.summarizedAt = null;
        this.sourceFingerprint = sourceFingerprint;
        this.sourceUpdatedAt = sourceUpdatedAt;
        this.model = model;
        this.promptVersion = promptVersion;
        this.retryCount = 0;
        this.nextAttemptAt = null;
        this.lockedUntil = null;
        this.workerId = null;
        this.failureReason = null;
    }

    public void markProcessing(String workerId, LocalDateTime lockedUntil) {
        this.status = ArticleAiSummaryStatus.PROCESSING;
        this.workerId = workerId;
        this.lockedUntil = lockedUntil;
        this.failureReason = null;
    }

    public void completeSuccess(
        List<String> summaryLines,
        String sourceFingerprint,
        LocalDateTime sourceUpdatedAt,
        String model,
        String promptVersion,
        LocalDateTime summarizedAt
    ) {
        this.status = ArticleAiSummaryStatus.SUCCESS;
        this.summaryContent = String.join("\n", summaryLines);
        this.summarizedAt = summarizedAt;
        this.sourceFingerprint = sourceFingerprint;
        this.sourceUpdatedAt = sourceUpdatedAt;
        this.model = model;
        this.promptVersion = promptVersion;
        this.nextAttemptAt = null;
        this.lockedUntil = null;
        this.workerId = null;
        this.failureReason = null;
    }

    public void completeFailure(String reason, LocalDateTime nextAttemptAt) {
        this.status = ArticleAiSummaryStatus.FAILED;
        this.retryCount++;
        this.nextAttemptAt = nextAttemptAt;
        this.lockedUntil = null;
        this.workerId = null;
        this.failureReason = truncate(reason);
    }

    public void skip(String reason) {
        this.status = ArticleAiSummaryStatus.SKIPPED;
        this.nextAttemptAt = null;
        this.lockedUntil = null;
        this.workerId = null;
        this.failureReason = truncate(reason);
    }

    private String truncate(String reason) {
        if (reason == null) {
            return null;
        }
        if (reason.length() <= FAILURE_REASON_LIMIT) {
            return reason;
        }
        return reason.substring(0, FAILURE_REASON_LIMIT);
    }
}
