package in.koreatech.koin.domain.dining.service;

import static in.koreatech.koin.global.code.ApiResponseCode.INVALID_CHANGE_CURSOR;
import static in.koreatech.koin.global.code.ApiResponseCode.INVALID_REPORT_FILTER;
import static in.koreatech.koin.global.code.ApiResponseCode.NOT_FOUND_DINING_REPORT;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.admin.dining.dto.AdminDiningReportResponse;
import in.koreatech.koin.common.model.Criteria;
import in.koreatech.koin.domain.dining.dto.DiningReportChangesResponse;
import in.koreatech.koin.domain.dining.dto.DiningReportPageResponse;
import in.koreatech.koin.domain.dining.dto.DiningReportResponse;
import in.koreatech.koin.domain.dining.dto.DiningReportSummaryResponse;
import in.koreatech.koin.domain.dining.model.DiningReport;
import in.koreatech.koin.domain.dining.model.DiningReportChange;
import in.koreatech.koin.domain.dining.model.DiningReportStatus;
import in.koreatech.koin.domain.dining.repository.DiningReportChangeRepository;
import in.koreatech.koin.domain.dining.repository.DiningReportRepository;
import in.koreatech.koin.domain.dining.repository.DiningReportSequenceRepository;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.repository.UserRepository;
import in.koreatech.koin.global.exception.CustomException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
public class DiningReportQueryService {

    private final DiningReportRepository reportRepository;
    private final DiningReportChangeRepository changeRepository;
    private final DiningReportSequenceRepository sequenceRepository;
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

    public DiningReportPageResponse<DiningReportResponse> getBotReports(Integer diningId, UUID processingId,
        DiningReportStatus status, Integer page, Integer limit) {
        if ((diningId == null) == (processingId == null) || (diningId != null && diningId < 1)) {
            throw CustomException.of(INVALID_REPORT_FILTER);
        }
        int total = Math.toIntExact(reportRepository.countReports(diningId, processingId, status));
        Criteria criteria = Criteria.of(page, limit, total);
        List<DiningReportResponse> rows = reportRepository.findReports(diningId, processingId, status,
                PageRequest.of(criteria.getPage(), criteria.getLimit(), Sort.by(Sort.Direction.ASC, "id")))
            .stream().map(DiningReportResponse::from).toList();
        return DiningReportPageResponse.of(rows, total, criteria);
    }

    public DiningReportChangesResponse getChanges(String cursor, Integer limit) {
        long after;
        try {
            if (cursor == null || !cursor.matches("[0-9]{1,19}")) {
                throw new IllegalArgumentException();
            }
            after = Long.parseLong(cursor);
        } catch (IllegalArgumentException exception) {
            throw CustomException.of(INVALID_CHANGE_CURSOR);
        }
        long last = sequenceRepository.findById(1)
            .orElseThrow(() -> new IllegalStateException("식단 제보 변경 순번 초기값이 없습니다.")).getLastSequence();
        if (after > last) {
            throw CustomException.of(INVALID_CHANGE_CURSOR);
        }
        int size = limit == null ? 50 : Math.max(1, Math.min(100, limit));
        List<DiningReportChange> found = changeRepository
            .findBySequenceGreaterThanAndSequenceLessThanEqualOrderBySequenceAsc(
                after, last, PageRequest.of(0, size + 1));
        boolean hasMore = found.size() > size;
        List<DiningReportChange> page = hasMore ? found.subList(0, size) : found;
        String next = page.isEmpty() ? cursor : page.get(page.size() - 1).getSequence().toString();
        return new DiningReportChangesResponse(page.stream().map(DiningReportChangesResponse.Change::from).toList(),
            next, hasMore);
    }

    private DiningReport getReport(Integer reportId) {
        return reportRepository.findById(reportId)
            .orElseThrow(() -> CustomException.of(NOT_FOUND_DINING_REPORT));
    }
}
