package in.koreatech.koin.domain.team.recruitment.repository;

import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitmentProfileSkill;
import org.springframework.data.repository.Repository;

import java.util.List;

public interface TeamRecruitmentProfileSkillRepository extends Repository<TeamRecruitmentProfileSkill, Integer> {

    TeamRecruitmentProfileSkill save(TeamRecruitmentProfileSkill skill);

    List<TeamRecruitmentProfileSkill> findAllByProfile_UserIdOrderByDisplayOrderAsc(Integer userId);

    void deleteAllByProfile_UserId(Integer userId);
}
