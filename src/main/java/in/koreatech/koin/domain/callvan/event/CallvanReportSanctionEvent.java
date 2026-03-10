package in.koreatech.koin.domain.callvan.event;

import in.koreatech.koin.domain.callvan.model.enums.CallvanReportProcessType;

public record CallvanReportSanctionEvent(
        Integer reportedUserId,
        Integer callvanPostId,
        CallvanReportProcessType processType
) {
}
