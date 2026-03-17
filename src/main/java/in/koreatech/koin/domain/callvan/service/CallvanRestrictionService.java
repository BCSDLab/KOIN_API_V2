package in.koreatech.koin.domain.callvan.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.callvan.repository.CallvanReportProcessRepository;
import in.koreatech.koin.global.code.ApiResponseCode;
import in.koreatech.koin.global.exception.CustomException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CallvanRestrictionService {

    private final CallvanReportProcessRepository callvanReportProcessRepository;

    public void validateNotRestricted(Integer userId) {
        boolean isRestricted = callvanReportProcessRepository.existsActiveRestrictionByReportedUserId(
            userId,
            LocalDateTime.now()
        );

        if (isRestricted) {
            throw CustomException.of(ApiResponseCode.FORBIDDEN_CALLVAN_RESTRICTED_USER);
        }
    }
}
