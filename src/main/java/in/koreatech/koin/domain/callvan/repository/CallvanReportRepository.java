package in.koreatech.koin.domain.callvan.repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import in.koreatech.koin.domain.callvan.model.CallvanReport;
import in.koreatech.koin.domain.callvan.model.enums.CallvanReportStatus;
import in.koreatech.koin.global.code.ApiResponseCode;
import in.koreatech.koin.global.exception.CustomException;

public interface CallvanReportRepository extends Repository<CallvanReport, Integer> {

    CallvanReport save(CallvanReport callvanReport);

    Optional<CallvanReport> findById(Integer id);

    default CallvanReport getById(Integer reportId) {
        return findById(reportId).orElseThrow(() -> CustomException.of(ApiResponseCode.NOT_FOUND_CALLVAN_REPORT));
    }

    boolean existsByPostIdAndReporterIdAndReportedIdAndStatusInAndIsDeletedFalse(
        Integer postId,
        Integer reporterId,
        Integer reportedId,
        List<CallvanReportStatus> statuses
    );

    @EntityGraph(attributePaths = "reported")
    Page<CallvanReport> findAllByStatusInAndIsDeletedFalse(List<CallvanReportStatus> statuses, Pageable pageable);

    long countByStatusInAndIsDeletedFalse(List<CallvanReportStatus> statuses);

    @EntityGraph(attributePaths = "reported")
    List<CallvanReport> findAllByReportedIdInAndStatusInAndIsDeletedFalseOrderByCreatedAtDesc(
        List<Integer> reportedIds,
        List<CallvanReportStatus> statuses
    );

    @Query("SELECT DISTINCT r.reported.id FROM CallvanReport r " +
        "WHERE r.post.id = :postId " +
        "AND r.status IN :statuses " +
        "AND r.isDeleted = false")
    Set<Integer> findReportedUserIdsByPostIdAndStatusIn(
        @Param("postId") Integer postId,
        @Param("statuses") List<CallvanReportStatus> statuses
    );

    @Query("SELECT DISTINCT r.reported.id FROM CallvanReport r " +
        "WHERE r.post.id = :postId " +
        "AND r.reporter.id = :reporterId " +
        "AND r.status IN :statuses " +
        "AND r.isDeleted = false")
    Set<Integer> findReportedUserIdsByPostIdAndReporterIdAndStatusIn(
        @Param("postId") Integer postId,
        @Param("reporterId") Integer reporterId,
        @Param("statuses") List<CallvanReportStatus> statuses);
}
