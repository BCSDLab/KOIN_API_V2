package in.koreatech.koin.domain.team.recruitment.repository;

import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitmentProfileActivity;
import org.springframework.data.repository.Repository;

import java.util.List;

public interface TeamRecruitmentProfileActivityRepository extends Repository<TeamRecruitmentProfileActivity, Integer> {

    TeamRecruitmentProfileActivity save(TeamRecruitmentProfileActivity activity);

    List<TeamRecruitmentProfileActivity> findAllByProfile_UserIdOrderByDisplayOrderAsc(Integer userId);

    void deleteAllByProfile_UserId(Integer userId);
}
