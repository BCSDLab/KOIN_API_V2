package in.koreatech.koin.admin.article.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.time.LocalDateTime;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.community.article.service.summary.ArticleSummaryFailureType;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(SnakeCaseStrategy.class)
public record AdminArticleAiSummaryResponse(
    @Schema(description = "요약 작업 ID", example = "1", requiredMode = REQUIRED)
    Integer summaryId,

    @Schema(description = "게시글 ID", example = "17291", requiredMode = REQUIRED)
    Integer articleId,

    @Schema(description = "게시판 ID", example = "5", requiredMode = REQUIRED)
    Integer boardId,

    @Schema(description = "게시글 제목", requiredMode = REQUIRED)
    String articleTitle,

    @Schema(description = "요약 상태", example = "FAILED", requiredMode = REQUIRED)
    String status,

    @Schema(description = "실패 유형", example = "RATE_LIMIT")
    ArticleSummaryFailureType failureType,

    @Schema(description = "안전하게 마스킹된 실패 메시지")
    String failureMessage,

    @Schema(description = "재시도 횟수", example = "2", requiredMode = REQUIRED)
    Integer retryCount,

    @Schema(description = "다음 시도 가능 시각")
    LocalDateTime nextAttemptAt,

    @Schema(description = "작업 잠금 만료 시각")
    LocalDateTime lockedUntil,

    @Schema(description = "작업자 ID")
    String workerId,

    @Schema(description = "생성 시각", requiredMode = REQUIRED)
    LocalDateTime createdAt,

    @Schema(description = "수정 시각", requiredMode = REQUIRED)
    LocalDateTime updatedAt,

    @Schema(description = "요약 성공 시각")
    LocalDateTime summarizedAt,

    @Schema(description = "원본 수정 시각")
    LocalDateTime sourceUpdatedAt,

    @Schema(description = "모델명")
    String model,

    @Schema(description = "프롬프트 버전")
    String promptVersion
) {
}
