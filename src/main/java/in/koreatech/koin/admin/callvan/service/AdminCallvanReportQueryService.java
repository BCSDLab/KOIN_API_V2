package in.koreatech.koin.admin.callvan.service;

import static in.koreatech.koin.domain.callvan.model.enums.CallvanReportStatus.*;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.admin.callvan.dto.AdminCallvanReportsResponse;
import in.koreatech.koin.admin.callvan.dto.CallvanReportPagedResult;
import in.koreatech.koin.admin.callvan.dto.CallvanReportRelatedData;
import in.koreatech.koin.common.model.Criteria;
import in.koreatech.koin.domain.callvan.model.CallvanReport;
import in.koreatech.koin.domain.callvan.model.CallvanReportAttachment;
import in.koreatech.koin.domain.callvan.model.CallvanReportProcess;
import in.koreatech.koin.domain.callvan.model.CallvanReportReason;
import in.koreatech.koin.domain.callvan.model.enums.CallvanReportStatus;
import in.koreatech.koin.domain.callvan.repository.CallvanReportAttachmentRepository;
import in.koreatech.koin.domain.callvan.repository.CallvanReportProcessRepository;
import in.koreatech.koin.domain.callvan.repository.CallvanReportReasonRepository;
import in.koreatech.koin.domain.callvan.repository.CallvanReportRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminCallvanReportQueryService {

    private static final List<CallvanReportStatus> ADMIN_VISIBLE_STATUSES = List.of(PENDING, CONFIRMED, REJECTED);
    private final CallvanReportRepository callvanReportRepository;
    private final CallvanReportProcessRepository callvanReportProcessRepository;
    private final CallvanReportReasonRepository callvanReportReasonRepository;
    private final CallvanReportAttachmentRepository callvanReportAttachmentRepository;

    @Transactional(readOnly = true)
    public AdminCallvanReportsResponse getReports(Boolean onlyPending, Integer page, Integer limit) {
        List<CallvanReportStatus> statuses =
            onlyPending ? List.of(PENDING) : ADMIN_VISIBLE_STATUSES;
        CallvanReportPagedResult pagedResult = getPagedReports(statuses, page, limit);
        CallvanReportRelatedData relatedData = loadRelatedData(pagedResult);
        return AdminCallvanReportsResponse.from(pagedResult, relatedData);
    }

    private CallvanReportPagedResult getPagedReports(List<CallvanReportStatus> statuses, Integer page, Integer limit) {
        int total = Math.toIntExact(callvanReportRepository.countByStatusInAndIsDeletedFalse(statuses));
        Criteria criteria = Criteria.of(page, limit, total);
        PageRequest pageable = PageRequest.of(
            criteria.getPage(),
            criteria.getLimit(),
            Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id"))
        );

        Page<CallvanReport> pagedReports = callvanReportRepository.findAllByStatusInAndIsDeletedFalse(
            statuses, pageable
        );

        return new CallvanReportPagedResult(
            pagedReports.getContent(), pagedReports.getTotalElements(),
            pagedReports.getTotalPages(), criteria.getPage() + 1
        );
    }

    private CallvanReportRelatedData loadRelatedData(CallvanReportPagedResult pagedResult) {
        List<CallvanReport> currentReports = pagedResult.reports();
        List<Integer> currentReportIds = currentReports.stream()
            .map(CallvanReport::getId)
            .toList();

        List<CallvanReport> accumulatedReports = fetchAccumulatedReports(currentReports);

        List<Integer> allReportIds = mergeDistinctIds(currentReportIds, accumulatedReports);

        return new CallvanReportRelatedData(
            loadReasonsMap(allReportIds),
            loadAttachmentsMap(currentReportIds),
            loadProcessMap(allReportIds),
            groupByReportedUserId(accumulatedReports));
    }

    private List<CallvanReport> fetchAccumulatedReports(List<CallvanReport> currentReports) {
        List<Integer> reportedUserIds = currentReports.stream()
            .map(report -> report.getReported().getId())
            .distinct()
            .toList();

        if (reportedUserIds.isEmpty()) {
            return List.of();
        }

        return callvanReportRepository.findAllByReportedIdInAndStatusInAndIsDeletedFalseOrderByCreatedAtDesc(
            reportedUserIds, ADMIN_VISIBLE_STATUSES);
    }

    private List<Integer> mergeDistinctIds(List<Integer> currentIds, List<CallvanReport> accumulatedReports) {
        return Stream.concat(
                currentIds.stream(),
                accumulatedReports.stream().map(CallvanReport::getId))
            .distinct()
            .toList();
    }

    private Map<Integer, List<CallvanReportReason>> loadReasonsMap(List<Integer> reportIds) {
        if (reportIds.isEmpty()) {
            return Map.of();
        }
        return groupByReportId(
            callvanReportReasonRepository.findAllByReportIdIn(reportIds),
            CallvanReportReason::getReport);
    }

    private Map<Integer, List<CallvanReportAttachment>> loadAttachmentsMap(List<Integer> reportIds) {
        if (reportIds.isEmpty()) {
            return Map.of();
        }
        return groupByReportId(
            callvanReportAttachmentRepository.findAllByReportIdIn(reportIds),
            CallvanReportAttachment::getReport);
    }

    private Map<Integer, CallvanReportProcess> loadProcessMap(List<Integer> reportIds) {
        if (reportIds.isEmpty()) {
            return Map.of();
        }
        return callvanReportProcessRepository.findAllByReportIdIn(reportIds).stream()
            .collect(Collectors.toMap(
                process -> process.getReport().getId(),
                Function.identity(),
                (left, right) -> left));
    }

    private Map<Integer, List<CallvanReport>> groupByReportedUserId(List<CallvanReport> reports) {
        return reports.stream()
            .collect(Collectors.groupingBy(
                report -> report.getReported().getId(),
                LinkedHashMap::new,
                Collectors.toList()));
    }

    private static <T> Map<Integer, List<T>> groupByReportId(
        Collection<T> values,
        Function<T, CallvanReport> reportExtractor) {
        return values.stream()
            .collect(Collectors.groupingBy(
                value -> reportExtractor.apply(value).getId(),
                LinkedHashMap::new,
                Collectors.toList()));
    }
}
