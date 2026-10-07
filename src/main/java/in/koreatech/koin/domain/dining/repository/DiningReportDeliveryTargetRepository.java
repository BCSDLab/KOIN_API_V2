package in.koreatech.koin.domain.dining.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import in.koreatech.koin.domain.dining.model.DiningReportDeliveryTarget;

public interface DiningReportDeliveryTargetRepository extends Repository<DiningReportDeliveryTarget, Integer> {

    DiningReportDeliveryTarget save(DiningReportDeliveryTarget target);

    Optional<DiningReportDeliveryTarget> findById(Integer reportId);

    @Query("""
        SELECT t FROM DiningReportDeliveryTarget t
        WHERE t.reportId > :afterReportId AND t.holdReason IS NULL
          AND (t.activeDeliveryId IS NOT NULL OR t.desiredSequence > t.confirmedSequence)
        ORDER BY t.reportId
        """)
    List<DiningReportDeliveryTarget> findCandidates(@Param("afterReportId") Integer afterReportId,
        Pageable pageable);
}
