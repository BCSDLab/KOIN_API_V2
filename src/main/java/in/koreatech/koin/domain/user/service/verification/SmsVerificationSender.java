package in.koreatech.koin.domain.user.service.verification;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin._common.event.UserSmsRequestEvent;
import in.koreatech.koin._common.util.random.CertificateNumberGenerator;
import in.koreatech.koin.domain.user.model.UserVerificationStatus;
import in.koreatech.koin.domain.user.repository.UserVerificationStatusRedisRepository;
import in.koreatech.koin.integration.naver.service.NaverSmsService;
import lombok.RequiredArgsConstructor;

@Component("sms")
@RequiredArgsConstructor
public class SmsVerificationSender implements VerificationSender {

    private static final long INITIAL_EXPIRATION_SECONDS = 60 * 3L;

    private final UserVerificationStatusRedisRepository userVerificationStatusRedisRepository;
    private final NaverSmsService naverSmsService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    @Override
    public void sendCode(String phoneNumber) {
        // sms 인증 코드 전송
        String verificationCode = CertificateNumberGenerator.generate();
        naverSmsService.sendVerificationCode(verificationCode, phoneNumber);

        // 미인증된 인증 상태 저장
        userVerificationStatusRedisRepository.save(
            UserVerificationStatus.of(phoneNumber, verificationCode, INITIAL_EXPIRATION_SECONDS));

        // 슬랙으로 메시지 전송
        eventPublisher.publishEvent(new UserSmsRequestEvent(phoneNumber));
    }
}
