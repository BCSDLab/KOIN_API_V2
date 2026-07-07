package in.koreatech.koin.admin.article.dto;

import java.time.LocalDateTime;

public interface AdminArticleAiSummaryLogProjection {

    Integer getLogId();

    Integer getSummaryId();

    Integer getArticleId();

    Integer getBoardId();

    String getArticleTitle();

    String getEventType();

    String getStatus();

    String getFailureType();

    String getMessage();

    Integer getRetryCount();

    LocalDateTime getNextAttemptAt();

    String getWorkerId();

    LocalDateTime getCreatedAt();
}
