package in.koreatech.koin.domain.callvan.service;

import java.time.Clock;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.callvan.dto.CallvanRestrictionResponse;
import in.koreatech.koin.domain.callvan.repository.CallvanReportProcessRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CallvanRestrictionQueryService {

    private final CallvanReportProcessRepository callvanReportProcessRepository;
    private final Clock clock;

    public CallvanRestrictionResponse getRestriction(Integer userId) {
        LocalDateTime now = LocalDateTime.now(clock);

        return callvanReportProcessRepository.findActiveRestrictionByReportedUserId(userId, now)
            .map(CallvanRestrictionResponse::from)
            .orElseGet(CallvanRestrictionResponse::unrestricted);
    }
}
