package in.koreatech.koin.domain.team.recruitment.dto;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentCategory;
import in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentMeetingType;
import in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentStatus;
import in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentType;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(SnakeCaseStrategy.class)
public record RecruitmentCard(
    @Schema(description = "모집글 ID", example = "17", requiredMode = REQUIRED)
    Integer id,

    @Schema(description = "모집 카테고리", example = "CONTEST", requiredMode = REQUIRED)
    TeamRecruitmentCategory category,

    @Schema(description = "모집글 제목", example = "AI 아이디어 공모전 팀원 모집", requiredMode = REQUIRED)
    String title,

    @Schema(description = "진행 방식", example = "ONLINE", requiredMode = REQUIRED)
    TeamRecruitmentMeetingType meetingType,

    @Schema(description = "활동 시작일", example = "2026-09-07", requiredMode = REQUIRED)
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate activityStartDate,

    @Schema(description = "활동 종료일", example = "2026-09-30", requiredMode = REQUIRED)
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate activityEndDate,

    @Schema(description = "지원 마감일", example = "2026-09-03", requiredMode = REQUIRED)
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate deadlineDate,

    @Schema(description = "D-day, 모집 마감 후 null", example = "8", nullable = true, requiredMode = REQUIRED)
    Integer dDay,

    @Schema(description = "모집 상태", example = "RECRUITING", requiredMode = REQUIRED)
    TeamRecruitmentStatus status,

    @Schema(description = "모집 유형", example = "ROLE_BASED", requiredMode = REQUIRED)
    TeamRecruitmentType recruitmentType,

    @Schema(description = "작성자를 제외한 승인된 전체 지원자 수", example = "2", requiredMode = REQUIRED)
    Integer currentParticipants,

    @Schema(description = "작성자를 제외한 전체 모집 정원", example = "5", requiredMode = REQUIRED)
    Integer maxParticipants,

    @Schema(description = "역할 목록", requiredMode = REQUIRED)
    List<RecruitmentRole> roles
) {
}
