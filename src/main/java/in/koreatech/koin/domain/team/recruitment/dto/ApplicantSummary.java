package in.koreatech.koin.domain.team.recruitment.dto;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentApplicationStatus;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(SnakeCaseStrategy.class)
public record ApplicantSummary(
    @Schema(description = "지원서 ID", example = "51", requiredMode = REQUIRED)
    Integer applicationId,

    @Schema(description = "지원자 닉네임", example = "김철수", requiredMode = REQUIRED)
    String nickname,

    @Schema(description = "지원자 학과", example = "컴퓨터공학부", requiredMode = REQUIRED)
    String department,

    @Schema(description = "지원자 입학 연도", example = "2023", requiredMode = REQUIRED)
    Integer studentYear,

    @Schema(description = "선택 역할, GENERAL 모집은 null", nullable = true, requiredMode = REQUIRED)
    ApplicationRole role,

    @Schema(description = "지원 상태", example = "PENDING", requiredMode = REQUIRED)
    TeamRecruitmentApplicationStatus status,

    @Schema(
        description = "기존 DIRECT 방이 있거나, 마감일이 지나지 않은 RECRUITING 상태이거나, "
            + "정원 충족으로 마감되어 ACTIVE TEAM 방이 있을 때 개인 채팅 시작 가능 여부",
        example = "false",
        requiredMode = REQUIRED
    )
    Boolean canOpenDirectChat
) {
}
