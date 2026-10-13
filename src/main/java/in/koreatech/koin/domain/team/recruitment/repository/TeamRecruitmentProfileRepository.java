package in.koreatech.koin.domain.team.recruitment.repository;

import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitmentProfile;
import org.springframework.data.repository.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface TeamRecruitmentProfileRepository extends Repository<TeamRecruitmentProfile, Integer> {

    TeamRecruitmentProfile save(TeamRecruitmentProfile profile);

    Optional<TeamRecruitmentProfile> findByUser_Id(Integer userId);

    List<TeamRecruitmentProfile> findAllByUserIdIn(Collection<Integer> userIds);

    boolean existsByUser_Id(Integer userId);
}
