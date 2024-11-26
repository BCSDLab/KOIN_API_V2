package in.koreatech.koin.domain.owner.service;

import in.koreatech.koin.domain.owner.model.dto.OwnerEmailRequestEvent;
import in.koreatech.koin.domain.owner.model.dto.OwnerSmsRequestEvent;
import in.koreatech.koin.domain.owner.model.redis.DailyVerificationLimit;
import in.koreatech.koin.domain.owner.model.redis.OwnerVerificationStatus;
import in.koreatech.koin.domain.owner.repository.redis.DailyVerificationLimitRepository;
import in.koreatech.koin.domain.owner.repository.redis.OwnerVerificationStatusRepository;
import in.koreatech.koin.global.domain.email.form.OwnerRegistrationData;
import in.koreatech.koin.global.domain.email.service.MailService;
import in.koreatech.koin.global.domain.random.model.CertificateNumberGenerator;
import in.koreatech.koin.global.exception.KoinIllegalArgumentException;
import in.koreatech.koin.global.naver.service.NaverSmsService;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OwnerVerificationService {

    private final ApplicationEventPublisher eventPublisher;
    private final OwnerVerificationStatusRepository ownerVerificationStatusRepository;
    private final DailyVerificationLimitRepository dailyVerificationLimitRedisRepository;
    private final NaverSmsService naverSmsService;
    private final MailService mailService;

    private void setVerificationCount(String key) {
        Optional<DailyVerificationLimit> dailyVerificationLimit = dailyVerificationLimitRedisRepository.findById(key);
        if (dailyVerificationLimit.isEmpty()) {
            dailyVerificationLimitRedisRepository.save(new DailyVerificationLimit(key));
        } else {
            DailyVerificationLimit dailyVerification = dailyVerificationLimit.get();
            dailyVerification.requestVerification();
            dailyVerificationLimitRedisRepository.save(dailyVerification);
        }
    }

    public void sendCertificationEmail(String email) {
        setVerificationCount(email);
        String certificationCode = CertificateNumberGenerator.generate();
        mailService.sendMail(email, new OwnerRegistrationData(certificationCode));
        OwnerVerificationStatus ownerVerificationStatus = new OwnerVerificationStatus(
                email,
                certificationCode
        );
        ownerVerificationStatusRepository.save(ownerVerificationStatus);
        eventPublisher.publishEvent(new OwnerEmailRequestEvent(email));
    }

    public void sendCertificationSms(String phoneNumber) {
        setVerificationCount(phoneNumber);
        String certificationCode = CertificateNumberGenerator.generate();
        naverSmsService.sendVerificationCode(certificationCode, phoneNumber);
        OwnerVerificationStatus ownerVerificationStatus = new OwnerVerificationStatus(
                phoneNumber,
                certificationCode
        );
        ownerVerificationStatusRepository.save(ownerVerificationStatus);
        eventPublisher.publishEvent(new OwnerSmsRequestEvent(phoneNumber));
    }

    public void verifyCode(String key, String code) {
        OwnerVerificationStatus verify = ownerVerificationStatusRepository.getByVerify(key);
        if (!Objects.equals(verify.getCertificationCode(), code)) {
            throw new KoinIllegalArgumentException("인증번호가 일치하지 않습니다.");
        }
        ownerVerificationStatusRepository.deleteById(key);
    }
}