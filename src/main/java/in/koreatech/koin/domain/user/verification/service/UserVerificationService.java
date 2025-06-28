package in.koreatech.koin.domain.user.verification.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin._common.auth.exception.AuthorizationException;
import in.koreatech.koin._common.event.UserEmailVerificationSendEvent;
import in.koreatech.koin._common.event.UserSmsVerificationSendEvent;
import in.koreatech.koin._common.exception.CustomException;
import in.koreatech.koin._common.exception.errorcode.ErrorCode;
import in.koreatech.koin._common.util.random.VerificationNumberGenerator;
import in.koreatech.koin.domain.user.verification.dto.SendVerificationResponse;
import in.koreatech.koin.domain.user.verification.model.VerificationCount;
import in.koreatech.koin.domain.user.verification.model.VerificationCode;
import in.koreatech.koin.domain.user.verification.model.VerificationChannel;
import in.koreatech.koin.domain.user.verification.repository.VerificationCountRedisRepository;
import in.koreatech.koin.domain.user.verification.repository.VerificationCodeRedisRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserVerificationService {

    private final VerificationCodeRedisRepository verificationCodeRedisRepository;
    private final VerificationCountRedisRepository verificationCountRedisRepository;
    private final VerificationNumberGenerator verificationNumberGenerator;
    private final ApplicationEventPublisher eventPublisher;

    @Value("${user.verification.max-verification-count:5}")
    private int maxVerificationCount;

    private VerificationCount increaseAndGetVerificationCount(String phoneNumberOrEmail, String ipAddress) {
        String countKey = VerificationCount.composeKey(phoneNumberOrEmail, ipAddress);
        VerificationCount count = verificationCountRedisRepository.findById(countKey)
            .orElseGet(() -> VerificationCount.of(phoneNumberOrEmail, ipAddress, maxVerificationCount));
        count.incrementVerificationCount();
        return verificationCountRedisRepository.save(count);
    }

    private void publishVerificationEvent(VerificationChannel channel, String code, String target) {
        switch (channel) {
            case SMS -> eventPublisher.publishEvent(new UserSmsVerificationSendEvent(code, target));
            case EMAIL -> eventPublisher.publishEvent(new UserEmailVerificationSendEvent(code, target));
        }
    }

    @Transactional
    public SendVerificationResponse sendVerification(String phoneNumberOrEmail, String ipAddress, VerificationChannel channel) {
        VerificationCount verificationCount = increaseAndGetVerificationCount(phoneNumberOrEmail, ipAddress);
        VerificationCode verificationCode = VerificationCode.of(phoneNumberOrEmail, verificationNumberGenerator, channel);
        verificationCodeRedisRepository.save(verificationCode);
        publishVerificationEvent(channel, verificationCode.getVerificationCode(), phoneNumberOrEmail);
        return SendVerificationResponse.from(verificationCount);
    }

    @Transactional
    public void verifyCode(String phoneNumberOrEmail, String ipAddress, String inputCode) {
        VerificationCode verificationCode = verificationCodeRedisRepository.findById(phoneNumberOrEmail)
            .orElseThrow(() -> AuthorizationException.withDetail("verification: " + phoneNumberOrEmail));
        verificationCode.detectAbnormalUsage();
        verificationCode.verify(inputCode);
        verificationCodeRedisRepository.save(verificationCode);
        String countKey = VerificationCount.composeKey(phoneNumberOrEmail, ipAddress);
        verificationCountRedisRepository.deleteById(countKey);
    }

    /**
     * <p>
     * 이 메서드는 지정된 전화번호 또는 이메일에 대한 본인 인증 상태가
     * <strong>완료되었는지 확인한 후</strong> 인증 정보를 레디스에서 삭제합니다.
     * </p>
     */
    @Transactional
    public void consumeVerification(String phoneNumberOrEmail) {
        VerificationCode verificationCode = verificationCodeRedisRepository.findById(phoneNumberOrEmail)
            .orElseThrow(() -> CustomException.of(ErrorCode.FORBIDDEN_API, "identity: " + phoneNumberOrEmail));
        verificationCode.requireVerified();
        verificationCodeRedisRepository.deleteById(phoneNumberOrEmail);
    }
}
