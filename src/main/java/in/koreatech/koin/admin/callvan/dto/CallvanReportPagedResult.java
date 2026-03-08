package in.koreatech.koin.admin.callvan.dto;

import java.util.List;

import in.koreatech.koin.domain.callvan.model.CallvanReport;

public record CallvanReportPagedResult(
    List<CallvanReport> reports,
    long totalCount,
    int totalPages,
    int currentPage
) {
}
