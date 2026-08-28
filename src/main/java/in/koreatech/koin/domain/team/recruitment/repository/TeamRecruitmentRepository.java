package in.koreatech.koin.domain.team.recruitment.repository;

import in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentStatus;
import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitment;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.time.LocalDate;
import java.util.Optional;

public interface TeamRecruitmentRepository extends Repository<TeamRecruitment, Integer> {

    TeamRecruitment save(TeamRecruitment recruitment);

    Optional<TeamRecruitment> findById(Integer id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT recruitment
        FROM TeamRecruitment recruitment
        WHERE recruitment.id = :id
        """)
    Optional<TeamRecruitment> findByIdWithLock(@Param("id") Integer id);

    Page<TeamRecruitment> findAllByStatusNot(TeamRecruitmentStatus status, Pageable pageable);

    Page<TeamRecruitment> findAllByStatusIn(Collection<TeamRecruitmentStatus> statuses, Pageable pageable);

    Page<TeamRecruitment> findAllByStatusAndDeadlineDateBefore(
        TeamRecruitmentStatus status,
        LocalDate deadlineDate,
        Pageable pageable
    );

    Page<TeamRecruitment> findAllByAuthor_Id(Integer authorId, Pageable pageable);

    Page<TeamRecruitment> findAllByAuthor_IdAndStatus(
        Integer authorId,
        TeamRecruitmentStatus status,
        Pageable pageable
    );

    long countByAuthor_IdAndStatus(Integer authorId, TeamRecruitmentStatus status);
}
