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
import in.koreatech.koin.domain.user.model.SmsAuthedStatus;
import in.koreatech.koin.domain.user.model.SmsDailyVerificationCount;
import in.koreatech.koin.domain.user.model.SmsVerificationStatus;
import in.koreatech.koin.domain.user.repository.SmsAuthedStatusRedisRepository;
import in.koreatech.koin.domain.user.repository.SmsDailyVerificationCountRedisRepository;
import in.koreatech.koin.domain.user.repository.SmsVerificationStatusRedisRepository;
import in.koreatech.koin.integration.naver.service.NaverSmsService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserSmsService {

    private final SmsVerificationStatusRedisRepository smsVerificationStatusRedisRepository;
    private final SmsDailyVerificationCountRedisRepository smsDailyVerificationCountRedisRepository;
    private final SmsAuthedStatusRedisRepository smsAuthedStatusRedisRepository;
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
        SmsDailyVerificationCount verificationCount = smsDailyVerificationCountRedisRepository.findByPhoneNumber(
                phoneNumber)
            .map(existing -> {
                existing.incrementVerificationCount();
                return existing;
            })
            .orElseGet(() -> SmsDailyVerificationCount.from(phoneNumber));

        // 횟수 증가 후 업데이트
        smsDailyVerificationCountRedisRepository.save(verificationCount);
    }

    private void sendSmsVerificationCode(String phoneNumber) {
        String verificationCode = CertificateNumberGenerator.generate();
        naverSmsService.sendVerificationCode(verificationCode, phoneNumber);
        smsVerificationStatusRedisRepository.save(SmsVerificationStatus.of(
            phoneNumber,
            verificationCode
        ));
    }

    @Transactional
    public void verifySmsCode(VerifySmsCodeRequest request) {
        // 인증 코드 미전송의 경우 400 반환
        SmsVerificationStatus verificationStatus = smsVerificationStatusRedisRepository.getByPhoneNumber(
            request.phoneNumber());

        // 인증 번호 일치 필터
        if (!Objects.equals(verificationStatus.getVerificationCode(), request.certificationCode())) {
            throw new KoinIllegalArgumentException("인증번호가 일치하지 않습니다.");
        }

        // 인증이 성공하면 인증 상태 삭제
        smsVerificationStatusRedisRepository.deleteByPhoneNumber(request.phoneNumber());

        // 인증 성공 상태 캐싱 (1시간)
        smsAuthedStatusRedisRepository.save(SmsAuthedStatus.from(request.phoneNumber()));
    }
}
