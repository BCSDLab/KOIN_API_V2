package in.koreatech.koin.domain.team.recruitment.repository;

import in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentApplicationStatus;
import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitmentApplication;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.Optional;

public interface TeamRecruitmentApplicationRepository extends Repository<TeamRecruitmentApplication, Integer> {

    TeamRecruitmentApplication save(TeamRecruitmentApplication application);

    Optional<TeamRecruitmentApplication> findById(Integer id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT application
        FROM TeamRecruitmentApplication application
        WHERE application.id = :id
        """)
    Optional<TeamRecruitmentApplication> findByIdWithLock(@Param("id") Integer id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT application
        FROM TeamRecruitmentApplication application
        WHERE application.id = :applicationId
        AND application.recruitment.id = :recruitmentId
        """)
    Optional<TeamRecruitmentApplication> findByIdAndRecruitmentIdWithLock(
        @Param("applicationId") Integer applicationId,
        @Param("recruitmentId") Integer recruitmentId
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT application
        FROM TeamRecruitmentApplication application
        WHERE application.recruitment.id = :recruitmentId
        AND application.applicant.id = :applicantId
        """)
    Optional<TeamRecruitmentApplication> findByRecruitmentIdAndApplicantIdWithLock(
        @Param("recruitmentId") Integer recruitmentId,
        @Param("applicantId") Integer applicantId
    );

    Optional<TeamRecruitmentApplication> findByRecruitment_IdAndApplicant_Id(Integer recruitmentId, Integer applicantId);

    Page<TeamRecruitmentApplication> findAllByApplicant_Id(Integer applicantId, Pageable pageable);

    Page<TeamRecruitmentApplication> findAllByApplicant_IdAndStatusIn(
        Integer applicantId,
        Collection<TeamRecruitmentApplicationStatus> statuses,
        Pageable pageable
    );

    Page<TeamRecruitmentApplication> findAllByRecruitment_IdAndStatusIn(
        Integer recruitmentId,
        Collection<TeamRecruitmentApplicationStatus> statuses,
        Pageable pageable
    );

    long countByApplicant_IdAndStatusIn(
        Integer applicantId,
        Collection<TeamRecruitmentApplicationStatus> statuses
    );

    long countByRecruitment_IdAndStatusIn(
        Integer recruitmentId,
        Collection<TeamRecruitmentApplicationStatus> statuses
    );

    long countByRecruitment_IdAndStatus(Integer recruitmentId, TeamRecruitmentApplicationStatus status);

    long countByRole_IdAndStatus(Integer roleId, TeamRecruitmentApplicationStatus status);
}
