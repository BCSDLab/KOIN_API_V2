package in.koreatech.koin.domain.user.service;

import java.util.Objects;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin._common.event.UserSmsRequestEvent;
import in.koreatech.koin._common.exception.custom.KoinIllegalArgumentException;
import in.koreatech.koin._common.util.random.CertificateNumberGenerator;
import in.koreatech.koin.domain.user.dto.SendSmsCodeRequest;
import in.koreatech.koin.domain.user.dto.VerifySmsCodeRequest;
import in.koreatech.koin.domain.user.exception.DuplicationPhoneNumberException;
import in.koreatech.koin.domain.user.model.SmsAuthedStatus;
import in.koreatech.koin.domain.user.model.UserDailyVerifyCount;
import in.koreatech.koin.domain.user.model.UserVerificationStatus;
import in.koreatech.koin.domain.user.repository.SmsAuthedStatusRedisRepository;
import in.koreatech.koin.domain.user.repository.UserRepository;
import in.koreatech.koin.domain.user.repository.UserVerificationLimitRedisRepository;
import in.koreatech.koin.domain.user.repository.UserVerificationStatusRedisRepository;
import in.koreatech.koin.integration.naver.service.NaverSmsService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserSmsService {

    private final UserVerificationStatusRedisRepository userVerificationStatusRedisRepository;
    private final UserVerificationLimitRedisRepository userVerificationLimitRedisRepository;
    private final SmsAuthedStatusRedisRepository smsAuthedStatusRedisRepository;
    private final UserRepository userRepository;
    private final NaverSmsService naverSmsService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void sendSmsCode(SendSmsCodeRequest request) {
        checkExistsPhoneNumber(request.phoneNumber());
        increaseUserDailyVerificationCount(request.phoneNumber());
        sendCertificationSms(request.phoneNumber());
        eventPublisher.publishEvent(new UserSmsRequestEvent(request.phoneNumber()));
    }

    private void checkExistsPhoneNumber(String phoneNumber) {
        userRepository.findByPhoneNumber(phoneNumber).ifPresent(user -> {
            throw DuplicationPhoneNumberException.withDetail("phone: " + user.getPhoneNumber());
        });
    }

    private void increaseUserDailyVerificationCount(String phoneNumber) {
        UserDailyVerifyCount limit = userVerificationLimitRedisRepository.findById(phoneNumber)
            .map(existing -> {
                existing.incrementVerificationCount();
                return existing;
            })
            .orElseGet(() -> UserDailyVerifyCount.from(phoneNumber));
        userVerificationLimitRedisRepository.save(limit);
    }

    private void sendCertificationSms(String phoneNumber) {
        String certificationCode = CertificateNumberGenerator.generate();
        naverSmsService.sendVerificationCode(certificationCode, phoneNumber);
        UserVerificationStatus userVerificationStatus = UserVerificationStatus.of(
            phoneNumber,
            certificationCode
        );
        userVerificationStatusRedisRepository.save(userVerificationStatus);
    }

    public void verifySmsCode(VerifySmsCodeRequest request) {
        UserVerificationStatus verify = userVerificationStatusRedisRepository.getById(request.phoneNumber());
        if (!Objects.equals(verify.getCertificationCode(), request.certificationCode())) {
            throw new KoinIllegalArgumentException("인증번호가 일치하지 않습니다.");
        }
        userVerificationStatusRedisRepository.deleteById(request.phoneNumber());
        SmsAuthedStatus smsAuthedStatus = SmsAuthedStatus.from(request.phoneNumber());
        smsAuthedStatusRedisRepository.save(smsAuthedStatus);
    }
}
