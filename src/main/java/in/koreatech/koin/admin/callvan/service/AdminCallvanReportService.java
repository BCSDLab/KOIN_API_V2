package in.koreatech.koin.admin.callvan.service;

import static in.koreatech.koin.domain.callvan.model.enums.CallvanReportStatus.PENDING;

import java.time.LocalDateTime;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.admin.callvan.dto.AdminCallvanReportProcessRequest;
import in.koreatech.koin.domain.callvan.event.CallvanReportWarningEvent;
import in.koreatech.koin.domain.callvan.model.CallvanReport;
import in.koreatech.koin.domain.callvan.model.CallvanReportProcess;
import in.koreatech.koin.domain.callvan.repository.CallvanReportProcessRepository;
import in.koreatech.koin.domain.callvan.repository.CallvanReportRepository;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.repository.UserRepository;
import in.koreatech.koin.global.code.ApiResponseCode;
import in.koreatech.koin.global.exception.CustomException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminCallvanReportService {

    private final CallvanReportRepository callvanReportRepository;
    private final CallvanReportProcessRepository callvanReportProcessRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void processReport(Integer reportId, Integer adminId, AdminCallvanReportProcessRequest request) {
        CallvanReport report = callvanReportRepository.getById(reportId);
        validateProcessable(report);

        User adminUser = userRepository.getById(adminId);
        LocalDateTime processedAt = LocalDateTime.now();

        CallvanReportProcess process = CallvanReportProcess.create(
            report, adminUser, request.processType(), request.processType().calculateRestrictedUntil(processedAt)
        );
        callvanReportProcessRepository.save(process);

        if (request.processType().isRejected()) {
            report.reject(adminUser);
            return;
        }

        report.confirm(adminUser);

        if (request.processType().isWarning()) {
            eventPublisher.publishEvent(
                new CallvanReportWarningEvent(report.getReported().getId(), report.getPost().getId()));
        }
    }

    private void validateProcessable(CallvanReport report) {
        if (report.getStatus() != PENDING
            || callvanReportProcessRepository.existsByReportIdAndIsDeletedFalse(report.getId())) {
            throw CustomException.of(ApiResponseCode.CALLVAN_REPORT_ALREADY_PROCESSED);
        }
    }
}
