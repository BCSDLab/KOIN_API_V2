package in.koreatech.koin.admin.article.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.time.LocalDateTime;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(SnakeCaseStrategy.class)
public record AdminArticleAiSummaryLogResponse(
    @Schema(description = "로그 ID", example = "1", requiredMode = REQUIRED)
    Integer logId,

    @Schema(description = "요약 작업 ID", example = "12")
    Integer summaryId,

    @Schema(description = "게시글 ID", example = "17291")
    Integer articleId,

    @Schema(description = "게시판 ID", example = "5")
    Integer boardId,

    @Schema(description = "게시글 제목")
    String articleTitle,

    @Schema(description = "로그 유형", example = "FAILED", requiredMode = REQUIRED)
    String eventType,

    @Schema(description = "로그 발생 시점의 요약 상태", example = "FAILED")
    String status,

    @Schema(description = "실패 유형", example = "RATE_LIMIT")
    String failureType,

    @Schema(description = "안전하게 마스킹된 메시지")
    String message,

    @Schema(description = "로그 발생 시점의 재시도 횟수", example = "2")
    Integer retryCount,

    @Schema(description = "다음 시도 가능 시각")
    LocalDateTime nextAttemptAt,

    @Schema(description = "작업자 ID")
    String workerId,

    @Schema(description = "로그 생성 시각", requiredMode = REQUIRED)
    LocalDateTime createdAt
) {
}
