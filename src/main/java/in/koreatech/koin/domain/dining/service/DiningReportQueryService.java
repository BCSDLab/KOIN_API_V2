package in.koreatech.koin.domain.dining.service;

import static in.koreatech.koin.global.code.ApiResponseCode.NOT_FOUND_DINING_REPORT;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.admin.dining.dto.AdminDiningReportResponse;
import in.koreatech.koin.common.model.Criteria;
import in.koreatech.koin.domain.dining.dto.DiningReportPageResponse;
import in.koreatech.koin.domain.dining.dto.DiningReportResponse;
import in.koreatech.koin.domain.dining.dto.DiningReportSummaryResponse;
import in.koreatech.koin.domain.dining.model.DiningReport;
import in.koreatech.koin.domain.dining.model.DiningReportStatus;
import in.koreatech.koin.domain.dining.repository.DiningReportRepository;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.repository.UserRepository;
import in.koreatech.koin.global.exception.CustomException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
public class DiningReportQueryService {

    private final DiningReportRepository reportRepository;
    private final UserRepository userRepository;

    public DiningReportResponse getBotReport(Integer reportId) {
        return DiningReportResponse.from(getReport(reportId));
    }

    public AdminDiningReportResponse getAdminReport(Integer reportId) {
        DiningReport report = getReport(reportId);
        User reporter = report.getReporterId() == null ? null
            : userRepository.findById(report.getReporterId()).orElse(null);
        return AdminDiningReportResponse.from(report, reporter);
    }

    public DiningReportPageResponse<DiningReportSummaryResponse> getAdminReports(
        boolean onlyPending, Integer page, Integer limit) {
        DiningReportStatus status = onlyPending ? DiningReportStatus.PENDING : null;
        int total = Math.toIntExact(reportRepository.countReports(null, null, status));
        Criteria criteria = Criteria.of(page, limit, total);
        List<DiningReportSummaryResponse> rows = reportRepository.findReports(null, null, status,
                PageRequest.of(criteria.getPage(), criteria.getLimit(), Sort.by(Sort.Direction.DESC, "createdAt", "id")))
            .stream().map(DiningReportSummaryResponse::from).toList();
        return DiningReportPageResponse.of(rows, total, criteria);
    }

    private DiningReport getReport(Integer reportId) {
        return reportRepository.findById(reportId)
            .orElseThrow(() -> CustomException.of(NOT_FOUND_DINING_REPORT));
    }
}
