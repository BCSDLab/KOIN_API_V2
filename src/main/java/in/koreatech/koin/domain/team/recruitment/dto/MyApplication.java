package in.koreatech.koin.domain.team.recruitment.dto;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentApplicationStatus;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(SnakeCaseStrategy.class)
public record MyApplication(
    @Schema(description = "지원서 ID", example = "51", requiredMode = REQUIRED)
    Integer applicationId,

    @Schema(description = "지원 상태", example = "ACCEPTED", requiredMode = REQUIRED)
    TeamRecruitmentApplicationStatus status,

    @Schema(description = "팀 채팅 이용 가능 여부", example = "true", requiredMode = REQUIRED)
    Boolean teamChatAvailable,

    @Schema(description = "팀 채팅방 ID", example = "31", nullable = true, requiredMode = REQUIRED)
    Integer teamChatRoomId,

    @Schema(description = "개인 채팅방 ID", example = "null", nullable = true, requiredMode = REQUIRED)
    Integer directChatRoomId,

    @Schema(description = "선택 역할, GENERAL 모집은 null", nullable = true, requiredMode = REQUIRED)
    ApplicationRole role,

    @Schema(description = "모집 카드", requiredMode = REQUIRED)
    RecruitmentCard recruitment
) {
}
