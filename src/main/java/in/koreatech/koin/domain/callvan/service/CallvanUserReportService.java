package in.koreatech.koin.domain.callvan.service;

import static in.koreatech.koin.domain.callvan.model.enums.CallvanReportStatus.PENDING;
import static in.koreatech.koin.domain.callvan.model.enums.CallvanReportStatus.UNDER_REVIEW;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.callvan.dto.CallvanUserReportCreateRequest;
import in.koreatech.koin.domain.callvan.model.CallvanPost;
import in.koreatech.koin.domain.callvan.model.CallvanReport;
import in.koreatech.koin.domain.callvan.model.enums.CallvanReportStatus;
import in.koreatech.koin.domain.callvan.repository.CallvanPostRepository;
import in.koreatech.koin.domain.callvan.repository.CallvanReportRepository;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.repository.UserRepository;
import in.koreatech.koin.global.code.ApiResponseCode;
import in.koreatech.koin.global.exception.CustomException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CallvanUserReportService {

    private static final List<CallvanReportStatus> ACTIVE_REPORT_STATUSES = List.of(PENDING, UNDER_REVIEW);

    private final CallvanPostRepository callvanPostRepository;
    private final UserRepository userRepository;
    private final CallvanReportRepository callvanReportRepository;

    @Transactional
    public void reportUser(
        Integer postId,
        Integer reporterId,
        CallvanUserReportCreateRequest request
    ) {
        CallvanPost callvanPost = callvanPostRepository.getById(postId);
        User reporter = userRepository.getById(reporterId);
        User reported = userRepository.getById(request.reportedUserId());

        callvanPost.verifyReportableParticipants(reporter.getId(), reported.getId());

        if (callvanReportRepository.existsByPostIdAndReporterIdAndReportedIdAndStatusInAndIsDeletedFalse(
            callvanPost.getId(),
            reporter.getId(),
            reported.getId(),
            ACTIVE_REPORT_STATUSES
        )) {
            throw CustomException.of(ApiResponseCode.CALLVAN_REPORT_ALREADY_PENDING);
        }

        CallvanReport callvanReport = CallvanReport.create(
            callvanPost,
            reporter,
            reported
        );

        callvanReport.registerReasons(
            request.reasons().stream()
                .map(reason -> new CallvanReport.CallvanReportReasonCreateCommand(
                    reason.reasonCode(),
                    reason.customText()
                ))
                .toList()
        );

        callvanReportRepository.save(callvanReport);
    }
}
