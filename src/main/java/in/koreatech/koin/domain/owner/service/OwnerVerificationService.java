package in.koreatech.koin.domain.owner.service;

import java.util.Objects;
import java.util.Optional;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import in.koreatech.koin.common.event.OwnerSmsVerificationSendEvent;
import in.koreatech.koin.global.exception.custom.KoinIllegalArgumentException;
import in.koreatech.koin.common.util.random.VerificationNumberGenerator;
import in.koreatech.koin.domain.owner.model.redis.DailyVerificationLimit;
import in.koreatech.koin.domain.owner.model.redis.OwnerVerificationStatus;
import in.koreatech.koin.domain.owner.repository.redis.DailyVerificationLimitRepository;
import in.koreatech.koin.domain.owner.repository.redis.OwnerVerificationStatusRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OwnerVerificationService {

    private final ApplicationEventPublisher eventPublisher;
    private final OwnerVerificationStatusRepository ownerVerificationStatusRepository;
    private final DailyVerificationLimitRepository dailyVerificationLimitRedisRepository;
    private final VerificationNumberGenerator verificationNumberGenerator;

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

    public void sendCertificationSms(String phoneNumber) {
        setVerificationCount(phoneNumber);
        String certificationCode = verificationNumberGenerator.generate();
        OwnerVerificationStatus ownerVerificationStatus = new OwnerVerificationStatus(
            phoneNumber,
            certificationCode
        );
        ownerVerificationStatusRepository.save(ownerVerificationStatus);
        eventPublisher.publishEvent(new OwnerSmsVerificationSendEvent(certificationCode, phoneNumber));
    }

    public void verifyCode(String key, String code) {
        OwnerVerificationStatus verify = ownerVerificationStatusRepository.getByVerify(key);
        if (!Objects.equals(verify.getCertificationCode(), code)) {
            throw new KoinIllegalArgumentException("인증번호가 일치하지 않습니다.");
        }
    }
}
