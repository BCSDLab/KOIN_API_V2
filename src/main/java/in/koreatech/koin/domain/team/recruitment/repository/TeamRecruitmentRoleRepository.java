package in.koreatech.koin.domain.team.recruitment.repository;

import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitmentRole;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TeamRecruitmentRoleRepository extends Repository<TeamRecruitmentRole, Integer> {

    TeamRecruitmentRole save(TeamRecruitmentRole role);

    Optional<TeamRecruitmentRole> findById(Integer id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT role
        FROM TeamRecruitmentRole role
        WHERE role.id = :id
        """)
    Optional<TeamRecruitmentRole> findByIdWithLock(@Param("id") Integer id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT role
        FROM TeamRecruitmentRole role
        WHERE role.id = :roleId
        AND role.recruitment.id = :recruitmentId
        """)
    Optional<TeamRecruitmentRole> findByIdAndRecruitmentIdWithLock(
        @Param("roleId") Integer roleId,
        @Param("recruitmentId") Integer recruitmentId
    );

    List<TeamRecruitmentRole> findAllByRecruitment_IdOrderByDisplayOrderAsc(Integer recruitmentId);

    long countByRecruitment_Id(Integer recruitmentId);
}
