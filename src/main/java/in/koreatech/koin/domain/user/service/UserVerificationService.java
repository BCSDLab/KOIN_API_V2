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
    private VerificationProcessor getVerificationProcessor(String phoneNumberOrEmail) {
        VerificationType verificationType = VerificationTypeDetector.detect(phoneNumberOrEmail);
        return verificationSenderMap.get(verificationType.getValue());
    }

    @Transactional
    public void sendCode(String phoneNumberOrEmail) {
        VerificationProcessor verificationProcessor = getVerificationProcessor(phoneNumberOrEmail);
        increaseUserDailyVerificationCount(phoneNumberOrEmail);
        verificationProcessor.sendCode(phoneNumberOrEmail);
    }

    private void increaseUserDailyVerificationCount(String phoneNumberOrEmail) {
        UserDailyVerificationCount verificationCount = userDailyVerificationCountRedisRepository.findById(phoneNumberOrEmail)
            .map(existing -> {
                existing.incrementVerificationCount();
                return existing;
            })
            .orElseGet(() -> UserDailyVerificationCount.from(phoneNumberOrEmail));
        userDailyVerificationCountRedisRepository.save(verificationCount);
    }

    @Transactional
    public void verifyCode(String phoneNumberOrEmail, String verificationCode) {
        UserVerificationStatus verificationStatus = userVerificationStatusRedisRepository.getById(
            phoneNumberOrEmail);
        if (verificationStatus.isVerified()) {
            return;
        }
        if (!Objects.equals(verificationStatus.getVerificationCode(), verificationCode)) {
            throw new KoinIllegalArgumentException("인증번호가 일치하지 않습니다.");
        }
        verificationStatus.markAsVerified();
        userVerificationStatusRedisRepository.save(verificationStatus);
    }

    public VerificationCountResponse getVerificationCount(String phoneNumberOrEmail) {
        return userDailyVerificationCountRedisRepository.findById(phoneNumberOrEmail)
            .map(verificationCount -> createVerificationCountResponse(phoneNumberOrEmail, verificationCount))
            .orElseGet(() -> createEmptyVerificationCountResponse(phoneNumberOrEmail));
    }

    private VerificationCountResponse createVerificationCountResponse(String phoneNumberOrEmail,
        UserDailyVerificationCount verificationCount) {
        int currentCount = verificationCount.getVerificationCount();
        int totalCount = UserDailyVerificationCount.MAX_VERIFICATION_COUNT;
        int remainingCount = totalCount - currentCount;
        return VerificationCountResponse.of(phoneNumberOrEmail, totalCount, remainingCount, currentCount);
    }

    private VerificationCountResponse createEmptyVerificationCountResponse(String phoneNumberOrEmail) {
        int maxCount = UserDailyVerificationCount.MAX_VERIFICATION_COUNT;
        return VerificationCountResponse.of(phoneNumberOrEmail, maxCount, maxCount, 0);
    }

    @Transactional
    public String findIdByVerification(String phoneNumberOrEmail) {
        checkVerified(phoneNumberOrEmail);
        VerificationProcessor verificationProcessor = getVerificationProcessor(phoneNumberOrEmail);
        String userId = verificationProcessor.findId(phoneNumberOrEmail);
        userVerificationStatusRedisRepository.deleteById(phoneNumberOrEmail);
        return userId;
    }

    @Transactional
    public void resetPasswordByVerification(String userId, String phoneNumberOrEmail, String newPassword) {
        checkVerified(phoneNumberOrEmail);
        VerificationProcessor verificationProcessor = getVerificationProcessor(phoneNumberOrEmail);
        verificationProcessor.resetPassword(userId, phoneNumberOrEmail, newPassword);
        userVerificationStatusRedisRepository.deleteById(phoneNumberOrEmail);
    }

    private void checkVerified(String phoneNumberOrEmail) {
        UserVerificationStatus userVerificationStatus = userVerificationStatusRedisRepository.getById(phoneNumberOrEmail);
        if (!userVerificationStatus.isVerified()) {
            throw new KoinIllegalArgumentException("유효하지 않은 인증 정보입니다.");
        }
    }
}
