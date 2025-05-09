package in.koreatech.koin.domain.user.verification.service;

import java.util.Optional;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin._common.auth.exception.AuthenticationException;
import in.koreatech.koin._common.event.UserEmailVerificationSendEvent;
import in.koreatech.koin._common.event.UserSmsVerificationSendEvent;
import in.koreatech.koin._common.exception.custom.KoinIllegalArgumentException;
import in.koreatech.koin._common.util.random.CertificateNumberGenerator;
import in.koreatech.koin.domain.user.verification.dto.SendVerificationResponse;
import in.koreatech.koin.domain.user.verification.model.UserDailyVerificationCount;
import in.koreatech.koin.domain.user.model.UserVerificationStatus;
import in.koreatech.koin.domain.user.verification.repository.UserDailyVerificationCountRedisRepository;
import in.koreatech.koin.domain.user.verification.repository.UserVerificationStatusRedisRepository;
import in.koreatech.koin._common.model.MailFormData;
import in.koreatech.koin.domain.user.verification.model.UserEmailVerificationData;
import in.koreatech.koin.infrastructure.email.service.MailService;
import in.koreatech.koin.infrastructure.naver.service.NaverSmsService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class UserVerificationService {

    private final UserVerificationStatusRedisRepository userVerificationStatusRedisRepository;
    private final UserDailyVerificationCountRedisRepository userDailyVerificationCountRedisRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final NaverSmsService naverSmsService;
    private final MailService mailService;

    public SendVerificationResponse sendSmsVerification(String phoneNumber) {
        String verificationCode = CertificateNumberGenerator.generate();
        naverSmsService.sendVerificationCode(verificationCode, phoneNumber);
        userVerificationStatusRedisRepository.save(UserVerificationStatus.ofSms(phoneNumber, verificationCode));
        eventPublisher.publishEvent(new UserSmsVerificationSendEvent(phoneNumber));
        UserDailyVerificationCount verificationCount = increaseUserDailyVerificationCount(phoneNumber);
        return SendVerificationResponse.from(verificationCount);
    }

    public SendVerificationResponse sendEmailVerification(String email) {
        String verificationCode = CertificateNumberGenerator.generate();
        MailFormData mailFormData = new UserEmailVerificationData(verificationCode);
        mailService.sendMail(email, mailFormData);
        userVerificationStatusRedisRepository.save(UserVerificationStatus.ofEmail(email, verificationCode));
        eventPublisher.publishEvent(new UserEmailVerificationSendEvent(email));
        UserDailyVerificationCount verificationCount = increaseUserDailyVerificationCount(email);
        return SendVerificationResponse.from(verificationCount);
    }

    public void verifyCode(String phoneNumberOrEmail, String verificationCode) {
        UserVerificationStatus verificationStatus = userVerificationStatusRedisRepository.getById(phoneNumberOrEmail);
        if (verificationStatus.isCodeMismatched(verificationCode)) {
            throw new KoinIllegalArgumentException("인증 번호가 일치하지 않습니다.");
        }
        verificationStatus.markAsVerified();
        userVerificationStatusRedisRepository.save(verificationStatus);
    }

    /**
     * <p>
     * 이 메서드는 지정된 전화번호 또는 이메일에 대한 본인 인증 상태가
     * <strong>완료되었는지 확인한 후</strong> 인증 정보를 레디스에서 삭제합니다.
     * </p>
     *
     * <p><b>주의:</b> 인증 정보는 이 메서드 호출 시 삭제되므로,
     * 인증이 필요한 비즈니스 로직이 모두 <strong>완료된 후 마지막에</strong> 호출해야 합니다.
     * 인증 상태를 1회성으로 "소비"하는 방식이며, 이후 동일한 인증 상태를 재사용할 수 없습니다.
     * 레디스는 트랜잭션을 지원하지 않으므로 메서드 내에서 오류 발생 시 롤백되지않습니다.
     * </p>
     */
    public void consumeVerification(String phoneNumberOrEmail) {
        userVerificationStatusRedisRepository.findById(phoneNumberOrEmail)
            .filter(UserVerificationStatus::isVerified)
            .orElseThrow(() -> new AuthenticationException("본인 인증 후 다시 시도해주십시오."));
        userVerificationStatusRedisRepository.deleteById(phoneNumberOrEmail);
    }

    private UserDailyVerificationCount increaseUserDailyVerificationCount(String phoneNumberOrEmail) {
        Optional<UserDailyVerificationCount> count = userDailyVerificationCountRedisRepository.findById(phoneNumberOrEmail);
        if (count.isEmpty()) {
            UserDailyVerificationCount newCount = UserDailyVerificationCount.from(phoneNumberOrEmail);
            return userDailyVerificationCountRedisRepository.save(newCount);
        }
        UserDailyVerificationCount updatedCount = count.get();
        updatedCount.incrementVerificationCount();
        return userDailyVerificationCountRedisRepository.save(updatedCount);
    }
}
