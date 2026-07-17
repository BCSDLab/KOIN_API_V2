package in.koreatech.koin.admin.article.dto;

public interface AdminArticleAiSummaryQueueCountProjection {

    Long getReadyWaitCount();

    Long getDelayedWaitCount();

    Long getProcessingCount();

    Long getExpiredProcessingCount();

    Long getRetryableFailedCount();
}
