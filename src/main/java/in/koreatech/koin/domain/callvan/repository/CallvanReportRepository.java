package in.koreatech.koin.domain.callvan.repository;

import java.util.List;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.callvan.model.CallvanReport;
import in.koreatech.koin.domain.callvan.model.enums.CallvanReportStatus;

public interface CallvanReportRepository extends Repository<CallvanReport, Integer> {

    CallvanReport save(CallvanReport callvanReport);

    boolean existsByPostIdAndReporterIdAndReportedIdAndStatusInAndIsDeletedFalse(
        Integer postId,
        Integer reporterId,
        Integer reportedId,
        List<CallvanReportStatus> statuses
    );
}
