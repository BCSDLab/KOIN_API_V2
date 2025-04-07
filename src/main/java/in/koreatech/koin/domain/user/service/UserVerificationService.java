package in.koreatech.koin.domain.user.service;

import static in.koreatech.koin.domain.user.service.verification.VerificationTypeDetector.detect;

import java.util.Map;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin._common.exception.custom.KoinIllegalArgumentException;
import in.koreatech.koin.domain.user.model.UserDailyVerificationCount;
import in.koreatech.koin.domain.user.model.UserVerificationStatus;
import in.koreatech.koin.domain.user.model.VerificationType;
import in.koreatech.koin.domain.user.repository.UserDailyVerificationCountRedisRepository;
import in.koreatech.koin.domain.user.repository.UserVerificationStatusRedisRepository;
import in.koreatech.koin.domain.user.service.verification.VerificationSender;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserVerificationService {

    private final Map<String, VerificationSender> verificationSenderMap;
    private final UserVerificationStatusRedisRepository userVerificationStatusRedisRepository;
    private final UserDailyVerificationCountRedisRepository userDailyVerificationCountRedisRepository;

    @Transactional
    public void sendCode(String target) {
        // 타겟의 형식으로 부터 sms or email 여부 판단
        VerificationType verificationType = detect(target);
        VerificationSender verificationSender = verificationSenderMap.get(verificationType.getValue());

        // 인증 횟수 증가 및 횟수 초과 시 400 반환
        increaseUserDailyVerificationCount(target);

        // 인증 코드 전송
        verificationSender.sendCode(target);
    }

    @Transactional
    public void verifyCode(String target, String code) {
        // 인증 코드 미전송의 경우 400 반환
        UserVerificationStatus verificationStatus = userVerificationStatusRedisRepository.getById(
            target);

        // 인증 번호 일치 확인
        if (!Objects.equals(verificationStatus.getVerificationCode(), code)) {
            throw new KoinIllegalArgumentException("인증번호가 일치하지 않습니다.");
        }

        // 인증 성공 처리 (verified = true, TTL 1시간)
        verificationStatus.markAsVerified();
        userVerificationStatusRedisRepository.save(verificationStatus);
    }

    private void increaseUserDailyVerificationCount(String target) {
        // 인증 횟수 증가
        UserDailyVerificationCount verificationCount = userDailyVerificationCountRedisRepository.findById(target)
            .map(existing -> {
                existing.incrementVerificationCount();
                return existing;
            })
            .orElseGet(() -> UserDailyVerificationCount.from(target));

        // 횟수 증가 후 업데이트
        userDailyVerificationCountRedisRepository.save(verificationCount);
    }
}
