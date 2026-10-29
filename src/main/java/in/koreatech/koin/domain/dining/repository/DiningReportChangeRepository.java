package in.koreatech.koin.domain.dining.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import in.koreatech.koin.domain.dining.model.DiningReportChange;

public interface DiningReportChangeRepository extends Repository<DiningReportChange, Long> {

    DiningReportChange save(DiningReportChange change);

    Optional<DiningReportChange> findByDeliveryId(UUID deliveryId);

    Optional<DiningReportChange> findByAttemptToken(UUID attemptToken);

    @Query("""
        SELECT c FROM DiningReportChange c
        WHERE (
            (c.deliveryState = in.koreatech.koin.domain.dining.model.DiningReportDeliveryStatus.QUEUED
                AND (c.nextAttemptAt IS NULL OR c.nextAttemptAt <= :now))
            OR (c.deliveryState = in.koreatech.koin.domain.dining.model.DiningReportDeliveryStatus.IN_PROGRESS
                AND c.expiresAt <= :now)
        ) AND NOT EXISTS (
            SELECT earlier.sequence FROM DiningReportChange earlier
            WHERE earlier.reportId = c.reportId AND earlier.sequence < c.sequence
                AND earlier.deliveryState <> in.koreatech.koin.domain.dining.model.DiningReportDeliveryStatus.DELIVERED
        )
        ORDER BY c.sequence
        """)
    List<DiningReportChange> findClaimable(@Param("now") LocalDateTime now, Pageable pageable);
}
