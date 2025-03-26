package in.koreatech.koin.domain.user.service;

import java.util.Optional;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin._common.event.UserSmsRequestEvent;
import in.koreatech.koin._common.util.random.CertificateNumberGenerator;
import in.koreatech.koin.domain.user.dto.SendSmsVerificationRequest;
import in.koreatech.koin.domain.user.model.UserDailyVerificationLimit;
import in.koreatech.koin.domain.user.model.UserVerificationStatus;
import in.koreatech.koin.domain.user.repository.UserVerificationLimitRedisRepository;
import in.koreatech.koin.domain.user.repository.UserVerificationStatusRedisRepository;
import in.koreatech.koin.integration.naver.service.NaverSmsService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserSmsService {

    private final NaverSmsService naverSmsService;
    private final UserVerificationStatusRedisRepository userVerificationStatusRedisRepository;
    private final UserVerificationLimitRedisRepository userVerificationLimitRedisRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void sendSignUpVerificationCode(SendSmsVerificationRequest request) {
        // Todo: 휴대폰 번호 중복 검사 로직
        sendCertificationSms(request.phoneNumber());
    }

    private void sendCertificationSms(String phoneNumber) {
        increaseUserDailyVerificationCount(phoneNumber);
        String certificationCode = CertificateNumberGenerator.generate();
        naverSmsService.sendVerificationCode(certificationCode, phoneNumber);
        UserVerificationStatus userVerificationStatus = new UserVerificationStatus(
            phoneNumber,
            certificationCode
        );
        userVerificationStatusRedisRepository.save(userVerificationStatus);
        eventPublisher.publishEvent(new UserSmsRequestEvent(phoneNumber));
    }

    private void increaseUserDailyVerificationCount(String key) {
        Optional<UserDailyVerificationLimit> optionalLimit = userVerificationLimitRedisRepository.findById(key);
        UserDailyVerificationLimit limit = optionalLimit
            .map(existing -> {
                existing.incrementVerificationCount();
                return existing;
            })
            .orElseGet(() -> new UserDailyVerificationLimit(key));
        userVerificationLimitRedisRepository.save(limit);
    }

    // public VerifySmsCodeResponse verifySignUpSmsCode(VerifySmsCodeRequest request) {
    //     // Todo: 코드 검증 로직
    // }
}
