package in.koreatech.koin.admin.article.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.util.List;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(SnakeCaseStrategy.class)
public record AdminArticleAiSummaryOverviewResponse(
    @Schema(description = "상태별 작업 수", requiredMode = REQUIRED)
    List<StatusCountResponse> statusCounts,

    @Schema(description = "큐 처리 상태", requiredMode = REQUIRED)
    QueueResponse queue,

    @Schema(description = "요약 설정", requiredMode = REQUIRED)
    ConfigResponse config
) {

    @JsonNaming(SnakeCaseStrategy.class)
    public record StatusCountResponse(
        @Schema(description = "요약 상태", example = "WAIT", requiredMode = REQUIRED)
        String status,

        @Schema(description = "작업 수", example = "12", requiredMode = REQUIRED)
        Long count
    ) {
    }

    @JsonNaming(SnakeCaseStrategy.class)
    public record QueueResponse(
        @Schema(description = "즉시 처리 가능한 WAIT 수", example = "4", requiredMode = REQUIRED)
        Long readyWaitCount,

        @Schema(description = "next_attempt_at 대기 중인 WAIT 수", example = "2", requiredMode = REQUIRED)
        Long delayedWaitCount,

        @Schema(description = "처리 중인 수", example = "1", requiredMode = REQUIRED)
        Long processingCount,

        @Schema(description = "lock이 만료된 PROCESSING 수", example = "0", requiredMode = REQUIRED)
        Long expiredProcessingCount,

        @Schema(description = "재시도 가능한 FAILED 수", example = "3", requiredMode = REQUIRED)
        Long retryableFailedCount
    ) {
    }

    @JsonNaming(SnakeCaseStrategy.class)
    public record ConfigResponse(
        @Schema(description = "스케줄러 실행 간격(ms)", example = "60000", requiredMode = REQUIRED)
        Long schedulerFixedDelayMs,

        @Schema(description = "스케줄러 batch size", example = "5", requiredMode = REQUIRED)
        Integer batchSize,

        @Schema(description = "최대 재시도 횟수", example = "5", requiredMode = REQUIRED)
        Integer maxRetryCount,

        @Schema(description = "FAILED 재처리 시작 시각", example = "0", requiredMode = REQUIRED)
        Integer failedRetryWindowStartHour,

        @Schema(description = "FAILED 재처리 종료 시각", example = "4", requiredMode = REQUIRED)
        Integer failedRetryWindowEndHour
    ) {
    }
}
