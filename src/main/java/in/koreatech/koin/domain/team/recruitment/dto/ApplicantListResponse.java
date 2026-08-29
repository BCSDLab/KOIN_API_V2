package in.koreatech.koin.domain.team.recruitment.dto;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.util.List;

import com.fasterxml.jackson.databind.annotation.JsonNaming;

import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(SnakeCaseStrategy.class)
public record ApplicantListResponse(
    @Schema(description = "모집 카드 및 팀 채팅 정보", requiredMode = REQUIRED)
    ApplicantRecruitment recruitment,

    @Schema(description = "지원자 목록", requiredMode = REQUIRED)
    List<ApplicantSummary> applications,

    @Schema(description = "전체 결과 수", example = "4", requiredMode = REQUIRED)
    Long totalCount,

    @Schema(description = "현재 페이지 결과 수", example = "4", requiredMode = REQUIRED)
    Integer currentCount,

    @Schema(description = "전체 페이지 수", example = "1", requiredMode = REQUIRED)
    Integer totalPage,

    @Schema(description = "현재 페이지", example = "1", requiredMode = REQUIRED)
    Integer currentPage
) {
}
