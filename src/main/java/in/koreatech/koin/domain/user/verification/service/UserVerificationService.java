package in.koreatech.koin.domain.user.verification.service;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin._common.auth.exception.AuthorizationException;
import in.koreatech.koin._common.event.UserEmailVerificationSendEvent;
import in.koreatech.koin._common.event.UserSmsVerificationSendEvent;
import in.koreatech.koin._common.exception.CustomException;
import in.koreatech.koin._common.code.ApiResponseCode;
import in.koreatech.koin._common.util.random.VerificationNumberGenerator;
import in.koreatech.koin.domain.user.verification.config.VerificationProperties;
import in.koreatech.koin.domain.user.verification.dto.SendVerificationResponse;
import in.koreatech.koin.domain.user.verification.model.UserDailyVerificationCount;
import in.koreatech.koin.domain.user.verification.model.UserVerificationStatus;
import in.koreatech.koin.domain.user.verification.repository.UserDailyVerificationCountRedisRepository;
import in.koreatech.koin.domain.user.verification.repository.UserVerificationStatusRedisRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserVerificationService {

    private final UserVerificationStatusRedisRepository userVerificationStatusRedisRepository;
    private final UserDailyVerificationCountRedisRepository userDailyVerificationCountRedisRepository;
    private final VerificationNumberGenerator verificationNumberGenerator;
    private final ApplicationEventPublisher eventPublisher;
    private final VerificationProperties verificationProperties;

    @Transactional
    public SendVerificationResponse sendSmsVerification(String phoneNumber) {
        UserDailyVerificationCount verificationCount = increaseAndGetUserDailyVerificationCount(phoneNumber);
        UserVerificationStatus userVerificationStatus = UserVerificationStatus.ofSms(phoneNumber, verificationNumberGenerator);
        userVerificationStatusRedisRepository.save(userVerificationStatus);
        eventPublisher.publishEvent(new UserSmsVerificationSendEvent(userVerificationStatus.getVerificationCode(), phoneNumber));
        return SendVerificationResponse.from(verificationCount);
    }

    @Transactional
    public SendVerificationResponse sendEmailVerification(String email) {
        UserDailyVerificationCount verificationCount = increaseAndGetUserDailyVerificationCount(email);
        UserVerificationStatus userVerificationStatus = UserVerificationStatus.ofEmail(email, verificationNumberGenerator);
        userVerificationStatusRedisRepository.save(userVerificationStatus);
        eventPublisher.publishEvent(new UserEmailVerificationSendEvent(userVerificationStatus.getVerificationCode(), email));
        return SendVerificationResponse.from(verificationCount);
    }

    @Transactional
    public void verifyCode(String phoneNumberOrEmail, String verificationCode) {
        UserVerificationStatus verificationStatus = userVerificationStatusRedisRepository.findById(phoneNumberOrEmail)
            .orElseThrow(() -> AuthorizationException.withDetail("verification: " + phoneNumberOrEmail));
        verificationStatus.verify(verificationCode);
        userVerificationStatusRedisRepository.save(verificationStatus);
    }

    /**
     * <p>
     * 이 메서드는 지정된 전화번호 또는 이메일에 대한 본인 인증 상태가
     * <strong>완료되었는지 확인한 후</strong> 인증 정보를 레디스에서 삭제합니다.
     * </p>
     */
    @Transactional
    public void consumeVerification(String phoneNumberOrEmail) {
        UserVerificationStatus verificationStatus = userVerificationStatusRedisRepository.findById(phoneNumberOrEmail)
            .orElseThrow(() -> CustomException.of(ApiResponseCode.FORBIDDEN_VERIFICATION, "identity: " + phoneNumberOrEmail));
        verificationStatus.requireVerified();
        userVerificationStatusRedisRepository.deleteById(phoneNumberOrEmail);
    }

    private UserDailyVerificationCount increaseAndGetUserDailyVerificationCount(String phoneNumberOrEmail) {
        UserDailyVerificationCount count = userDailyVerificationCountRedisRepository.findById(phoneNumberOrEmail)
            .orElseGet(() -> UserDailyVerificationCount.of(phoneNumberOrEmail, verificationProperties));
        count.incrementVerificationCount();
        return userDailyVerificationCountRedisRepository.save(count);
    }
}
