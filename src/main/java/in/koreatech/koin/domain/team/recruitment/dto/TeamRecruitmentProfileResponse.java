package in.koreatech.koin.domain.team.recruitment.dto;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.util.List;

import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.student.model.Major;
import in.koreatech.koin.domain.student.model.Student;
import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitmentProfile;
import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitmentProfileActivity;
import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitmentProfileSkill;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(SnakeCaseStrategy.class)
public record TeamRecruitmentProfileResponse(
    @Schema(description = "팀원 모집 전용 닉네임", example = "홍길동", requiredMode = REQUIRED)
    String profileNickname,

    @Schema(description = "학적 정보의 학과/학부", example = "컴퓨터공학부", requiredMode = REQUIRED)
    String department,

    @Schema(description = "학적 정보의 전공. 전공 구분이 없는 학부는 null입니다.", example = "null",
        nullable = true, requiredMode = REQUIRED)
    String major,

    @Schema(description = "학적 정보의 학번", example = "2023100000", requiredMode = REQUIRED)
    String studentNumber,

    @Schema(description = "선호 역할", example = "기획", requiredMode = REQUIRED)
    String preferredRole,

    @Schema(description = "보유 기술", example = "[\"정보처리기사\"]", requiredMode = REQUIRED)
    List<String> skills,

    @Schema(description = "활동 내역", requiredMode = REQUIRED)
    List<ProfileActivity> activities,

    @Schema(description = "자기소개", example = "안녕하세요.", requiredMode = REQUIRED)
    String selfIntroduction
) {
    public static TeamRecruitmentProfileResponse of(TeamRecruitmentProfile profile, Student student) {
        return new TeamRecruitmentProfileResponse(
            profile.getProfileNickname(),
            student.getDepartment() == null ? null : student.getDepartment().getName(),
            majorNameOf(student.getMajor()),
            student.getStudentNumber(),
            profile.getPreferredRole(),
            profile.getSkills().stream()
                .map(TeamRecruitmentProfileSkill::getSkill)
                .toList(),
            profile.getActivities().stream()
                .map(TeamRecruitmentProfileResponse::activityOf)
                .toList(),
            profile.getSelfIntroduction()
        );
    }

    private static String majorNameOf(Major major) {
        return major == null ? null : major.getName();
    }

    private static ProfileActivity activityOf(TeamRecruitmentProfileActivity activity) {
        return new ProfileActivity(
            activity.getId(),
            activity.getTitle(),
            activity.getStartedAt(),
            activity.getEndedAt(),
            activity.getIsOngoing(),
            activity.getDescription()
        );
    }
}
