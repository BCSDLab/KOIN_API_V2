package in.koreatech.koin.domain.user.service;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin._common.exception.custom.KoinIllegalArgumentException;
import in.koreatech.koin.domain.user.dto.VerificationCountResponse;
import in.koreatech.koin.domain.user.model.UserDailyVerificationCount;
import in.koreatech.koin.domain.user.model.UserVerificationStatus;
import in.koreatech.koin.domain.user.model.VerificationType;
import in.koreatech.koin.domain.user.repository.UserDailyVerificationCountRedisRepository;
import in.koreatech.koin.domain.user.repository.UserVerificationStatusRedisRepository;
import in.koreatech.koin.domain.user.service.verification.VerificationSender;
import in.koreatech.koin.domain.user.service.verification.VerificationTypeDetector;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserVerificationService {

    private final Map<String, VerificationSender> verificationSenderMap;
    private final UserVerificationStatusRedisRepository userVerificationStatusRedisRepository;
    private final UserDailyVerificationCountRedisRepository userDailyVerificationCountRedisRepository;

    @Transactional
    public void sendCode(String target) {
        VerificationType verificationType = VerificationTypeDetector.detect(target);
        VerificationSender verificationSender = verificationSenderMap.get(verificationType.getValue());
        increaseUserDailyVerificationCount(target);
        verificationSender.sendCode(target);
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
        Optional<UserDailyVerificationCount> verificationCountOpt = userDailyVerificationCountRedisRepository.findById(
            target);
        if (verificationCountOpt.isEmpty()) {
            int maxCount = UserDailyVerificationCount.MAX_VERIFICATION_COUNT;
            return VerificationCountResponse.of(target, maxCount, maxCount, 0);
        }
        UserDailyVerificationCount verificationCount = verificationCountOpt.get();
        int currentCount = verificationCount.getVerificationCount();
        int totalCount = UserDailyVerificationCount.MAX_VERIFICATION_COUNT;
        int remainingCount = totalCount - currentCount;
        return VerificationCountResponse.of(target, totalCount, remainingCount, currentCount);
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
}
