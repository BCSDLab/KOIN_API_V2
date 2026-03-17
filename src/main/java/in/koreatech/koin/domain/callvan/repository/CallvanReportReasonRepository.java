package in.koreatech.koin.domain.callvan.repository;

import java.util.List;

import org.springframework.data.repository.Repository;
import in.koreatech.koin.domain.callvan.model.CallvanReportReason;

public interface CallvanReportReasonRepository extends Repository<CallvanReportReason, Integer> {

    List<CallvanReportReason> findAllByReportIdIn(List<Integer> reportIds);
}
