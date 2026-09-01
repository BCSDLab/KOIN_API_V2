package in.koreatech.koin.domain.team.recruitment.dto;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentApplicationStatus;
import in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentApplyBlockReason;
import in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentCategory;
import in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentMeetingType;
import in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentStatus;
import in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentType;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(SnakeCaseStrategy.class)
public record RecruitmentDetail(
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

    @Schema(description = "D-day. 모집 상태가 RECRUITING이고 마감일이 오늘 또는 미래일 때만 반환되며, CLOSED/DELETED, null 마감일 또는 마감일 경과 시 null입니다.",
        example = "8", nullable = true, requiredMode = REQUIRED)
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
    List<RecruitmentRole> roles,

    @Schema(description = "작성자 닉네임", example = "홍길동", requiredMode = REQUIRED)
    String authorNickname,

    @Schema(description = "모집 상세 설명", example = "공모전 팀원을 모집합니다.", requiredMode = REQUIRED)
    String description,

    @Schema(description = "관련 링크", example = "https://example.com", nullable = true, requiredMode = REQUIRED)
    String relatedUrl,

    @Schema(description = "지원 자격", example = "기획 경험이 있는 분", nullable = true, requiredMode = REQUIRED)
    String qualification,

    @Schema(description = "작성 일시(KST)", example = "2026-08-25 09:00:00", requiredMode = REQUIRED)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime createdAt,

    @Schema(description = "조회자가 작성자인지 여부", example = "false", requiredMode = REQUIRED)
    Boolean isAuthor,

    @Schema(description = "지원 가능 여부", example = "true", requiredMode = REQUIRED)
    Boolean canApply,

    @Schema(description = "지원 불가 사유. 지원 가능하면 null입니다.", nullable = true, requiredMode = REQUIRED)
    TeamRecruitmentApplyBlockReason applyBlockReason,

    @Schema(description = "내 지원 정보. 미지원자는 null입니다.", nullable = true, requiredMode = REQUIRED)
    AppliedApplication application,

    @Schema(description = "지원자 관리 가능 여부", example = "false", requiredMode = REQUIRED)
    Boolean canManageApplicants,

    @Schema(description = "팀 채팅방 이용 가능 여부", example = "false", requiredMode = REQUIRED)
    Boolean teamChatAvailable,

    @Schema(description = "팀 채팅방 ID. 이용 가능할 때만 non-null입니다.", example = "31",
        nullable = true, requiredMode = REQUIRED)
    Integer teamChatRoomId
) {
    @JsonNaming(SnakeCaseStrategy.class)
    public record AppliedApplication(
        @Schema(description = "지원서 ID", example = "51", requiredMode = REQUIRED)
        Integer applicationId,

        @Schema(description = "지원 상태", example = "PENDING", requiredMode = REQUIRED)
        TeamRecruitmentApplicationStatus status
    ) {
    }
}
