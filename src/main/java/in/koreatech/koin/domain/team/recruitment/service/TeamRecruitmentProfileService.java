package in.koreatech.koin.domain.team.recruitment.service;

import static in.koreatech.koin.global.code.ApiResponseCode.TEAM_RECRUITMENT_PROFILE_NOT_FOUND;

import java.util.List;
import java.util.stream.IntStream;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.student.model.Student;
import in.koreatech.koin.domain.student.repository.StudentRepository;
import in.koreatech.koin.domain.team.recruitment.dto.ProfileActivityInput;
import in.koreatech.koin.domain.team.recruitment.dto.TeamRecruitmentProfileResponse;
import in.koreatech.koin.domain.team.recruitment.dto.TeamRecruitmentProfileUpsertRequest;
import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitmentProfile;
import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitmentProfileActivity;
import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitmentProfileSkill;
import in.koreatech.koin.domain.team.recruitment.repository.TeamRecruitmentProfileRepository;
import in.koreatech.koin.global.exception.CustomException;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TeamRecruitmentProfileService {

    // display_order 는 CHECK 제약이 1 이상을 요구한다.
    private static final int FIRST_DISPLAY_ORDER = 1;

    private final TeamRecruitmentProfileRepository teamRecruitmentProfileRepository;
    private final StudentRepository studentRepository;
    private final EntityManager entityManager;

    public TeamRecruitmentProfileResponse getMyProfile(Integer userId) {
        TeamRecruitmentProfile profile = teamRecruitmentProfileRepository.findByUser_Id(userId)
            .orElseThrow(() -> CustomException.of(TEAM_RECRUITMENT_PROFILE_NOT_FOUND, "userId: " + userId));
        return TeamRecruitmentProfileResponse.of(profile, studentRepository.getById(userId));
    }

    @Transactional
    public TeamRecruitmentProfileResponse upsertMyProfile(
        Integer userId,
        TeamRecruitmentProfileUpsertRequest request
    ) {
        Student student = studentRepository.getById(userId);
        TeamRecruitmentProfile profile = teamRecruitmentProfileRepository.findByUser_Id(userId)
            .orElseGet(() -> createProfile(student, request));
        profile.replace(request.profileNickname(), request.preferredRole(), request.selfIntroduction());
        replaceSkillsAndActivities(profile, request);
        entityManager.flush();
        return TeamRecruitmentProfileResponse.of(profile, student);
    }

    private TeamRecruitmentProfile createProfile(Student student, TeamRecruitmentProfileUpsertRequest request) {
        return teamRecruitmentProfileRepository.save(
            TeamRecruitmentProfile.builder()
                .user(student.getUser())
                .profileNickname(request.profileNickname())
                .preferredRole(request.preferredRole())
                .selfIntroduction(request.selfIntroduction())
                .build()
        );
    }

    /**
     * 기술과 활동 내역은 (profile_user_id, display_order)에 unique 제약이 있다.
     * 기존 행을 지우기 전에 새 행이 insert 되면 제약을 위반하므로,
     * 목록을 비운 뒤 flush 해서 delete 를 먼저 내보낸다.
     */
    private void replaceSkillsAndActivities(
        TeamRecruitmentProfile profile,
        TeamRecruitmentProfileUpsertRequest request
    ) {
        profile.replaceSkills(List.of());
        profile.replaceActivities(List.of());
        entityManager.flush();

        profile.replaceSkills(toSkills(request.skills()));
        profile.replaceActivities(toActivities(request.activities()));
    }

    private List<TeamRecruitmentProfileSkill> toSkills(List<String> skills) {
        return IntStream.range(0, skills.size())
            .mapToObj(index -> TeamRecruitmentProfileSkill.builder()
                .skill(skills.get(index))
                .displayOrder(index + FIRST_DISPLAY_ORDER)
                .build())
            .toList();
    }

    private List<TeamRecruitmentProfileActivity> toActivities(List<ProfileActivityInput> activities) {
        return IntStream.range(0, activities.size())
            .mapToObj(index -> toActivity(activities.get(index), index + FIRST_DISPLAY_ORDER))
            .toList();
    }

    private TeamRecruitmentProfileActivity toActivity(ProfileActivityInput input, int displayOrder) {
        return TeamRecruitmentProfileActivity.builder()
            .title(input.title())
            .startedAt(input.startedAt())
            .endedAt(input.endedAt())
            .isOngoing(input.isOngoing())
            .description(input.description())
            .displayOrder(displayOrder)
            .build();
    }
}
