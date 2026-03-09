package in.koreatech.koin.domain.callvan.repository;

import java.util.List;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.callvan.model.CallvanReportAttachment;

public interface CallvanReportAttachmentRepository extends Repository<CallvanReportAttachment, Integer> {

    List<CallvanReportAttachment> findAllByReportIdIn(List<Integer> reportIds);
}
