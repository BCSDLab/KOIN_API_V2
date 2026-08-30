package in.koreatech.koin.domain.team.recruitment.dto;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentCategory;
import in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentMeetingType;
import in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@JsonNaming(SnakeCaseStrategy.class)
public record UpdateRecruitmentRequest(
    @Schema(description = "모집 카테고리", example = "CONTEST", requiredMode = REQUIRED)
    @NotNull
    TeamRecruitmentCategory category,

    @Schema(description = "모집글 제목", example = "AI 아이디어 공모전 팀원 모집", requiredMode = REQUIRED)
    @NotBlank
    @Size(min = 1, max = 50)
    String title,

    @Schema(description = "진행 방식", example = "ONLINE", requiredMode = REQUIRED)
    @NotNull
    TeamRecruitmentMeetingType meetingType,

    @Schema(description = "활동 시작일", example = "2026-09-07", requiredMode = REQUIRED)
    @JsonFormat(pattern = "yyyy-MM-dd")
    @NotNull
    LocalDate activityStartDate,

    @Schema(description = "활동 종료일", example = "2026-09-30", requiredMode = REQUIRED)
    @JsonFormat(pattern = "yyyy-MM-dd")
    @NotNull
    LocalDate activityEndDate,

    @Schema(description = "지원 마감일. 활동 시작일 이하여야 합니다.", example = "2026-09-03", requiredMode = REQUIRED)
    @JsonFormat(pattern = "yyyy-MM-dd")
    @NotNull
    LocalDate deadlineDate,

    @Schema(description = "모집 유형", example = "ROLE_BASED", requiredMode = REQUIRED)
    @NotNull
    TeamRecruitmentType recruitmentType,

    @Schema(description = "전체 모집 정원. GENERAL 모집만 사용하며 ROLE_BASED 모집은 역할 정원의 합으로 계산됩니다.",
        example = "5", nullable = true, requiredMode = NOT_REQUIRED)
    Integer maxParticipants,

    @Schema(description = "역할 목록. 기존 역할은 id를 반드시 보내고 새 역할은 id를 생략합니다. "
        + "ROLE_BASED 모집은 1~5개이며 역할별 정원의 합은 최대 10명입니다. "
        + "GENERAL 모집은 빈 배열입니다.", requiredMode = REQUIRED)
    @NotNull
    List<@NotNull @Valid UpdateRoleInput> roles,

    @Schema(description = "모집 상세 설명", example = "공모전 팀원을 모집합니다.", requiredMode = REQUIRED)
    @NotBlank
    @Size(min = 1, max = 1000)
    String description,

    @Schema(description = "관련 링크", example = "https://example.com", nullable = true, requiredMode = NOT_REQUIRED)
    @Pattern(regexp = "^https://.*")
    @Size(max = 2048)
    String relatedUrl,

    @Schema(description = "지원 자격", example = "기획 경험이 있는 분", nullable = true, requiredMode = NOT_REQUIRED)
    @Size(min = 1, max = 500)
    String qualification
) {
    public UpdateRecruitmentRequest {
        RecruitmentRequestValidator.validatePeriod(activityStartDate, activityEndDate, deadlineDate);
        RecruitmentRequestValidator.validateRoleComposition(recruitmentType, roles, maxParticipants);
        if (RecruitmentRequestValidator.isCompleteRoleList(roles, UpdateRoleInput::isComplete)) {
            RecruitmentRequestValidator.validateDistinctRoleNames(roles.stream().map(UpdateRoleInput::name).toList());
            RecruitmentRequestValidator.validateTotalCapacity(
                recruitmentType == TeamRecruitmentType.ROLE_BASED
                    ? roles.stream().mapToInt(UpdateRoleInput::maxParticipants).sum()
                    : (maxParticipants == null ? 0 : maxParticipants));
        }
    }

    public int resolveMaxParticipants() {
        if (recruitmentType == TeamRecruitmentType.ROLE_BASED) {
            return roles.stream().mapToInt(UpdateRoleInput::maxParticipants).sum();
        }
        return maxParticipants;
    }
}
