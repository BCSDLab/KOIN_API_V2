package in.koreatech.koin.domain.user.service;

import java.util.Objects;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin._common.auth.JwtProvider;
import in.koreatech.koin._common.event.UserSmsRequestEvent;
import in.koreatech.koin._common.exception.custom.KoinIllegalArgumentException;
import in.koreatech.koin._common.util.random.CertificateNumberGenerator;
import in.koreatech.koin.domain.user.dto.SendSmsVerificationRequest;
import in.koreatech.koin.domain.user.dto.VerifySmsCodeRequest;
import in.koreatech.koin.domain.user.dto.VerifySmsCodeResponse;
import in.koreatech.koin.domain.user.model.UserDailyVerifyCount;
import in.koreatech.koin.domain.user.model.UserVerificationStatus;
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
    private final NaverSmsService naverSmsService;
    private final ApplicationEventPublisher eventPublisher;
    private final JwtProvider jwtProvider;

    @Transactional
    public void sendSignUpVerificationCode(SendSmsVerificationRequest request) {
        // Todo: 휴대폰 번호 중복 검사 로직
        sendCertificationSms(request.phoneNumber());
    }

    private void sendCertificationSms(String phoneNumber) {
        increaseUserDailyVerificationCount(phoneNumber);
        String certificationCode = CertificateNumberGenerator.generate();
        naverSmsService.sendVerificationCode(certificationCode, phoneNumber);
        UserVerificationStatus userVerificationStatus = UserVerificationStatus.of(
            phoneNumber,
            certificationCode
        );
        userVerificationStatusRedisRepository.save(userVerificationStatus);
        eventPublisher.publishEvent(new UserSmsRequestEvent(phoneNumber));
    }

    private void increaseUserDailyVerificationCount(String phoneNumber) {
        UserDailyVerifyCount limit = userVerificationLimitRedisRepository.findById(phoneNumber)
            .map(existing -> {
                existing.incrementVerificationCount();
                return existing;
            })
            .orElseGet(() -> new UserDailyVerifyCount(phoneNumber));
        userVerificationLimitRedisRepository.save(limit);
    }

    public VerifySmsCodeResponse verifySignUpSmsCode(VerifySmsCodeRequest request) {
        UserVerificationStatus verify = userVerificationStatusRedisRepository.getById(request.phoneNumber());
        if (!Objects.equals(verify.getCertificationCode(), request.certificationCode())) {
            throw new KoinIllegalArgumentException("인증번호가 일치하지 않습니다.");
        }
        return new VerifySmsCodeResponse(jwtProvider.createTemporaryToken());
    }
}
