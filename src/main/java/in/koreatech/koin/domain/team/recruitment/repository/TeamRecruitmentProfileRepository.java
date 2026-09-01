package in.koreatech.koin.domain.team.recruitment.repository;

import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitmentProfile;
import org.springframework.data.repository.Repository;

import java.util.Optional;

public interface TeamRecruitmentProfileRepository extends Repository<TeamRecruitmentProfile, Integer> {

    TeamRecruitmentProfile save(TeamRecruitmentProfile profile);

    Optional<TeamRecruitmentProfile> findByUser_Id(Integer userId);

    boolean existsByUser_Id(Integer userId);
}
