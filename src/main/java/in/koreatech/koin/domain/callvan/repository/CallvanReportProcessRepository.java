package in.koreatech.koin.domain.callvan.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import in.koreatech.koin.domain.callvan.model.CallvanReportProcess;

public interface CallvanReportProcessRepository extends Repository<CallvanReportProcess, Integer> {

    CallvanReportProcess save(CallvanReportProcess process);

    List<CallvanReportProcess> findAllByReportIdIn(List<Integer> reportIds);

    boolean existsByReportIdAndIsDeletedFalse(Integer reportId);

    @Query("""
        SELECT CASE WHEN COUNT(process) > 0 THEN true ELSE false END
        FROM CallvanReportProcess process
        WHERE process.report.reported.id = :userId
          AND process.isDeleted = false
          AND (
              process.processType = in.koreatech.koin.domain.callvan.model.enums.CallvanReportProcessType.PERMANENT_RESTRICTION
              OR (
                  process.processType = in.koreatech.koin.domain.callvan.model.enums.CallvanReportProcessType.TEMPORARY_RESTRICTION_14_DAYS
                  AND process.restrictedUntil IS NOT NULL
                  AND process.restrictedUntil >= :now
              )
          )
        """)
    boolean existsActiveRestrictionByReportedUserId(
        @Param("userId") Integer userId,
        @Param("now") LocalDateTime now
    );
}
