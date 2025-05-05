package in.koreatech.koin.domain.user.service;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin._common.auth.exception.AuthenticationException;
import in.koreatech.koin._common.event.UserEmailVerificationSendEvent;
import in.koreatech.koin._common.event.UserSmsVerificationSendEvent;
import in.koreatech.koin._common.exception.custom.KoinIllegalArgumentException;
import in.koreatech.koin._common.util.random.CertificateNumberGenerator;
import in.koreatech.koin.domain.user.dto.verification.SendVerificationResponse;
import in.koreatech.koin.domain.user.model.UserDailyVerificationCount;
import in.koreatech.koin.domain.user.model.UserVerificationStatus;
import in.koreatech.koin.domain.user.repository.UserDailyVerificationCountRedisRepository;
import in.koreatech.koin.domain.user.repository.UserVerificationStatusRedisRepository;
import in.koreatech.koin._common.model.MailFormData;
import in.koreatech.koin.domain.user.model.UserEmailVerificationData;
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
        UserDailyVerificationCount verificationCount = increaseUserDailyVerificationCount(phoneNumber);
        String verificationCode = CertificateNumberGenerator.generate();
        naverSmsService.sendVerificationCode(verificationCode, phoneNumber);
        userVerificationStatusRedisRepository.save(UserVerificationStatus.ofSms(phoneNumber, verificationCode));
        eventPublisher.publishEvent(new UserSmsVerificationSendEvent(phoneNumber));
        return SendVerificationResponse.from(verificationCount);
    }

    public SendVerificationResponse sendEmailVerification(String email) {
        UserDailyVerificationCount verificationCount = increaseUserDailyVerificationCount(email);
        String verificationCode = CertificateNumberGenerator.generate();
        MailFormData mailFormData = new UserEmailVerificationData(verificationCode);
        mailService.sendMail(email, mailFormData);
        userVerificationStatusRedisRepository.save(UserVerificationStatus.ofEmail(email, verificationCode));
        eventPublisher.publishEvent(new UserEmailVerificationSendEvent(email));
        return SendVerificationResponse.from(verificationCount);
    }

    private UserDailyVerificationCount increaseUserDailyVerificationCount(String phoneNumberOrEmail) {
        UserDailyVerificationCount verificationCount = userDailyVerificationCountRedisRepository.findById(
                phoneNumberOrEmail)
            .map(existing -> {
                existing.incrementVerificationCount();
                return existing;
            })
            .orElseGet(() -> UserDailyVerificationCount.from(phoneNumberOrEmail));
        return userDailyVerificationCountRedisRepository.save(verificationCount);
    }

    public void verifyCode(String phoneNumberOrEmail, String verificationCode) {
        UserVerificationStatus verificationStatus = userVerificationStatusRedisRepository.getById(phoneNumberOrEmail);
        if (verificationStatus.isCodeMismatched(verificationCode)) {
            throw new KoinIllegalArgumentException("인증 번호가 일치하지 않습니다.");
        }
        verificationStatus.markAsVerified();
        userVerificationStatusRedisRepository.save(verificationStatus);
    }

    public void checkVerified(String phoneNumber) {
        userVerificationStatusRedisRepository.findById(phoneNumber)
            .filter(UserVerificationStatus::isVerified)
            .orElseThrow(() -> new AuthenticationException("본인 인증 후 다시 시도해주십시오."));
        userVerificationStatusRedisRepository.deleteById(phoneNumber);
    }
}
