package in.koreatech.koin.domain.team.recruitment.dto;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentApplicationStatus;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(SnakeCaseStrategy.class)
public record ApplicantDetail(
    @Schema(description = "지원서 ID", example = "51", requiredMode = REQUIRED)
    Integer applicationId,

    @Schema(description = "지원 상태", example = "PENDING", requiredMode = REQUIRED)
    TeamRecruitmentApplicationStatus status,

    @Schema(description = "지원 당시 프로필 snapshot", requiredMode = REQUIRED)
    ProfileSnapshot profileSnapshot,

    @Schema(description = "지원 동기", example = "지원 동기입니다.", requiredMode = REQUIRED)
    String motivation,

    @Schema(description = "가능 시간", example = "월수금 20시 이후", requiredMode = REQUIRED)
    String availability,

    @Schema(description = "선택 역할, GENERAL 모집은 null", nullable = true, requiredMode = REQUIRED)
    ApplicationRole role,

    @Schema(description = "현재 사용자가 승인/거절할 수 있는지", example = "true", requiredMode = REQUIRED)
    Boolean canDecide,

    @Schema(
        description = "기존 DIRECT 방이 있거나, 마감일이 지나지 않은 RECRUITING 상태이거나, "
            + "정원 충족으로 마감되어 ACTIVE TEAM 방이 있을 때 개인 채팅을 시작할 수 있는지",
        example = "false",
        requiredMode = REQUIRED
    )
    Boolean canOpenDirectChat
) {
}
