package in.koreatech.koin.domain.dining.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import in.koreatech.koin.domain.dining.model.DiningReport;
import in.koreatech.koin.domain.dining.model.DiningReportStatus;
import jakarta.persistence.LockModeType;

public interface DiningReportRepository extends Repository<DiningReport, Integer> {

    DiningReport saveAndFlush(DiningReport report);

    Optional<DiningReport> findById(Integer id);

    @Query("SELECT r.dining.id FROM DiningReport r WHERE r.id = :id")
    Optional<Integer> findDiningIdById(@Param("id") Integer id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM DiningReport r WHERE r.dining.id = :diningId ORDER BY r.id")
    List<DiningReport> findAllByDiningIdForUpdate(@Param("diningId") Integer diningId);

    @Query("""
        SELECT COUNT(r) FROM DiningReport r
        WHERE (:diningId IS NULL OR r.dining.id = :diningId)
          AND (:processingId IS NULL OR r.processingId = :processingId)
          AND (:status IS NULL OR r.status = :status)
        """)
    long countReports(@Param("diningId") Integer diningId, @Param("processingId") UUID processingId,
        @Param("status") DiningReportStatus status);

    @Query("""
        SELECT r FROM DiningReport r JOIN FETCH r.dining
        WHERE (:diningId IS NULL OR r.dining.id = :diningId)
          AND (:processingId IS NULL OR r.processingId = :processingId)
          AND (:status IS NULL OR r.status = :status)
        """)
    List<DiningReport> findReports(@Param("diningId") Integer diningId,
        @Param("processingId") UUID processingId, @Param("status") DiningReportStatus status, Pageable pageable);
}
