package in.koreatech.koin.domain.team.recruitment.repository;

import in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentOutboxEventStatus;
import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitmentOutboxEvent;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Collection;

public interface TeamRecruitmentOutboxEventRepository extends Repository<TeamRecruitmentOutboxEvent, Integer> {

    TeamRecruitmentOutboxEvent save(TeamRecruitmentOutboxEvent event);

    Optional<TeamRecruitmentOutboxEvent> findByEventKey(String eventKey);

    Optional<TeamRecruitmentOutboxEvent> findById(Integer id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT event
        FROM TeamRecruitmentOutboxEvent event
        WHERE event.id = :id
        """)
    Optional<TeamRecruitmentOutboxEvent> findByIdWithLock(@Param("id") Integer id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT event
        FROM TeamRecruitmentOutboxEvent event
        WHERE event.status = :status
        AND (event.nextAttemptAt IS NULL OR event.nextAttemptAt <= :now)
        ORDER BY event.id ASC
        """)
    List<TeamRecruitmentOutboxEvent> findReadyForPublish(
        @Param("status") TeamRecruitmentOutboxEventStatus status,
        @Param("now") LocalDateTime now,
        Pageable pageable
    );

    @Query(value = """
        SELECT *
        FROM team_recruitment_outbox_event
        WHERE (
            (
                status IN ('PENDING', 'FAILED')
                AND attempt_count < :maxAttempts
                AND (next_attempt_at IS NULL OR next_attempt_at <= :now)
            )
            OR (
                status = 'PROCESSING'
                AND (locked_until IS NULL OR locked_until < :now)
            )
        )
        AND event_type = 'TEAM_RECRUITMENT_NOTIFICATION'
        ORDER BY id ASC
        LIMIT :limit
        FOR UPDATE SKIP LOCKED
        """, nativeQuery = true)
    List<TeamRecruitmentOutboxEvent> findClaimableForUpdate(
        @Param("now") LocalDateTime now,
        @Param("limit") int limit,
        @Param("maxAttempts") int maxAttempts
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT event
        FROM TeamRecruitmentOutboxEvent event
        WHERE event.status IN :statuses
        AND (event.nextAttemptAt IS NULL OR event.nextAttemptAt <= :now)
        ORDER BY event.id ASC
        """)
    List<TeamRecruitmentOutboxEvent> findReadyForPublish(
        @Param("statuses") Collection<TeamRecruitmentOutboxEventStatus> statuses,
        @Param("now") LocalDateTime now,
        Pageable pageable
    );

    List<TeamRecruitmentOutboxEvent> findAllByStatusOrderByIdAsc(TeamRecruitmentOutboxEventStatus status);
}
