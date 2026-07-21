package in.koreatech.koin.admin.article.dto;

import java.time.LocalDateTime;

public interface AdminArticleAiSummaryProjection {

    Integer getSummaryId();

    Integer getArticleId();

    Integer getBoardId();

    String getArticleTitle();

    String getStatus();

    String getFailureReason();

    Integer getRetryCount();

    LocalDateTime getNextAttemptAt();

    LocalDateTime getLockedUntil();

    String getWorkerId();

    LocalDateTime getCreatedAt();

    LocalDateTime getUpdatedAt();

    LocalDateTime getSummarizedAt();

    LocalDateTime getSourceUpdatedAt();

    String getModel();

    String getPromptVersion();
}
