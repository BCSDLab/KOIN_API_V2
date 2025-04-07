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
import in.koreatech.koin.domain.user.model.UserDailyVerificationCount;
import in.koreatech.koin.domain.user.model.UserVerificationStatus;
import in.koreatech.koin.domain.user.repository.UserDailyVerificationCountRedisRepository;
import in.koreatech.koin.domain.user.repository.UserVerificationStatusRedisRepository;
import in.koreatech.koin.integration.naver.service.NaverSmsService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserSmsService {

    private final UserVerificationStatusRedisRepository userVerificationStatusRedisRepository;
    private final UserDailyVerificationCountRedisRepository userDailyVerificationCountRedisRepository;
    private final NaverSmsService naverSmsService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void sendSmsCode(SendSmsCodeRequest request) {
        // 인증 횟수 증가 및 횟수 초과 시 400 반환
        increaseUserDailyVerificationCount(request.phoneNumber());

        // sms 인증 코드 전송
        sendSmsVerificationCode(request.phoneNumber());

        // 슬랙으로 메시지 전송
        eventPublisher.publishEvent(new UserSmsRequestEvent(request.phoneNumber()));
    }

    private void increaseUserDailyVerificationCount(String phoneNumber) {
        // 인증 횟수 증가
        UserDailyVerificationCount verificationCount = userDailyVerificationCountRedisRepository.findById(phoneNumber)
            .map(existing -> {
                existing.incrementVerificationCount();
                return existing;
            })
            .orElseGet(() -> UserDailyVerificationCount.from(phoneNumber));

        // 횟수 증가 후 업데이트
        userDailyVerificationCountRedisRepository.save(verificationCount);
    }

    private void sendSmsVerificationCode(String phoneNumber) {
        String verificationCode = CertificateNumberGenerator.generate();
        naverSmsService.sendVerificationCode(verificationCode, phoneNumber);
        userVerificationStatusRedisRepository.save(UserVerificationStatus.of(phoneNumber, verificationCode));
    }

    @Transactional
    public void verifySmsCode(VerifySmsCodeRequest request) {
        // 인증 코드 미전송의 경우 400 반환
        UserVerificationStatus verificationStatus = userVerificationStatusRedisRepository.getById(
            request.phoneNumber());

        // 인증 번호 일치 확인
        if (!Objects.equals(verificationStatus.getVerificationCode(), request.certificationCode())) {
            throw new KoinIllegalArgumentException("인증번호가 일치하지 않습니다.");
        }

        // 인증 성공 처리 (verified = true, TTL 1시간)
        verificationStatus.markAsVerified();
        userVerificationStatusRedisRepository.save(verificationStatus);
    }
}
