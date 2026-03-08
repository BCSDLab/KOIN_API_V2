package in.koreatech.koin.admin.callvan.dto;

import java.util.List;
import java.util.Map;

import in.koreatech.koin.domain.callvan.model.CallvanReport;
import in.koreatech.koin.domain.callvan.model.CallvanReportAttachment;
import in.koreatech.koin.domain.callvan.model.CallvanReportProcess;
import in.koreatech.koin.domain.callvan.model.CallvanReportReason;

public record CallvanReportRelatedData(
    Map<Integer, List<CallvanReportReason>> reasonsByReportId,
    Map<Integer, List<CallvanReportAttachment>> attachmentsByReportId,
    Map<Integer, CallvanReportProcess> processByReportId,
    Map<Integer, List<CallvanReport>> accumulatedReportsByUserId
) {

    public List<CallvanReportReason> reasons(Integer reportId) {
        return reasonsByReportId.getOrDefault(reportId, List.of());
    }

    public List<CallvanReportAttachment> attachments(Integer reportId) {
        return attachmentsByReportId.getOrDefault(reportId, List.of());
    }

    public CallvanReportProcess process(Integer reportId) {
        return processByReportId.get(reportId);
    }

    public List<CallvanReport> accumulatedReports(Integer reportedUserId) {
        return accumulatedReportsByUserId.getOrDefault(reportedUserId, List.of());
    }
}
