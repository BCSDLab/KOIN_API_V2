package in.koreatech.koin.domain.user.service;

import java.util.Map;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin._common.exception.custom.KoinIllegalArgumentException;
import in.koreatech.koin.domain.user.dto.verification.VerificationCountResponse;
import in.koreatech.koin.domain.user.model.UserDailyVerificationCount;
import in.koreatech.koin.domain.user.model.UserVerificationStatus;
import in.koreatech.koin.domain.user.model.VerificationType;
import in.koreatech.koin.domain.user.repository.UserDailyVerificationCountRedisRepository;
import in.koreatech.koin.domain.user.repository.UserVerificationStatusRedisRepository;
import in.koreatech.koin.domain.user.service.verification.VerificationProcessor;
import in.koreatech.koin.domain.user.service.verification.VerificationTypeDetector;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserVerificationService {

    private final Map<String, VerificationProcessor> verificationSenderMap;
    private final UserVerificationStatusRedisRepository userVerificationStatusRedisRepository;
    private final UserDailyVerificationCountRedisRepository userDailyVerificationCountRedisRepository;

    // 인증수단(휴대폰 or 이메일)에 따른 구현체 반환 메서드
    private VerificationProcessor getVerificationProcessor(String target) {
        VerificationType verificationType = VerificationTypeDetector.detect(target);
        return verificationSenderMap.get(verificationType.getValue());
    }

    @Transactional
    public void sendCode(String target) {
        VerificationProcessor verificationProcessor = getVerificationProcessor(target);
        increaseUserDailyVerificationCount(target);
        verificationProcessor.sendCode(target);
    }

    private void increaseUserDailyVerificationCount(String target) {
        UserDailyVerificationCount verificationCount = userDailyVerificationCountRedisRepository.findById(target)
            .map(existing -> {
                existing.incrementVerificationCount();
                return existing;
            })
            .orElseGet(() -> UserDailyVerificationCount.from(target));
        userDailyVerificationCountRedisRepository.save(verificationCount);
    }

    @Transactional
    public void verifyCode(String target, String code) {
        UserVerificationStatus verificationStatus = userVerificationStatusRedisRepository.getById(
            target);
        if (verificationStatus.isVerified()) {
            return;
        }
        if (!Objects.equals(verificationStatus.getVerificationCode(), code)) {
            throw new KoinIllegalArgumentException("인증번호가 일치하지 않습니다.");
        }
        verificationStatus.markAsVerified();
        userVerificationStatusRedisRepository.save(verificationStatus);
    }

    public VerificationCountResponse getVerificationCount(String target) {
        return userDailyVerificationCountRedisRepository.findById(target)
            .map(verificationCount -> createVerificationCountResponse(target, verificationCount))
            .orElseGet(() -> createEmptyVerificationCountResponse(target));
    }

    private VerificationCountResponse createVerificationCountResponse(String target,
        UserDailyVerificationCount verificationCount) {
        int currentCount = verificationCount.getVerificationCount();
        int totalCount = UserDailyVerificationCount.MAX_VERIFICATION_COUNT;
        int remainingCount = totalCount - currentCount;
        return VerificationCountResponse.of(target, totalCount, remainingCount, currentCount);
    }

    private VerificationCountResponse createEmptyVerificationCountResponse(String target) {
        int maxCount = UserDailyVerificationCount.MAX_VERIFICATION_COUNT;
        return VerificationCountResponse.of(target, maxCount, maxCount, 0);
    }

    @Transactional
    public String findIdByVerification(String target) {
        checkVerified(target);
        VerificationProcessor verificationProcessor = getVerificationProcessor(target);
        String userId = verificationProcessor.findId(target);
        userVerificationStatusRedisRepository.deleteById(target);
        return userId;
    }

    @Transactional
    public void resetPasswordByVerification(String userId, String target, String newPassword) {
        checkVerified(target);
        VerificationProcessor verificationProcessor = getVerificationProcessor(target);
        verificationProcessor.resetPassword(userId, target, newPassword);
        userVerificationStatusRedisRepository.deleteById(target);
    }

    private void checkVerified(String target) {
        UserVerificationStatus userVerificationStatus = userVerificationStatusRedisRepository.getById(target);
        if (!userVerificationStatus.isVerified()) {
            throw new KoinIllegalArgumentException("유효하지 않은 인증 정보입니다.");
        }
    }
}
