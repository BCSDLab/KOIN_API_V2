package in.koreatech.koin.domain.owner.service;

import in.koreatech.koin._common.event.OwnerSmsRequestEvent;
import in.koreatech.koin.domain.owner.model.redis.DailyVerificationLimit;
import in.koreatech.koin.domain.owner.model.redis.OwnerVerificationStatus;
import in.koreatech.koin.domain.owner.repository.redis.DailyVerificationLimitRepository;
import in.koreatech.koin.domain.owner.repository.redis.OwnerVerificationStatusRepository;
import in.koreatech.koin._common.util.random.CertificateNumberGenerator;
import in.koreatech.koin._common.exception.custom.KoinIllegalArgumentException;
import in.koreatech.koin.integration.naver.service.NaverSmsService;
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
    }
}
