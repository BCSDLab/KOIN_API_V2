package in.koreatech.koin.domain.user.service;

import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin._common.exception.custom.KoinIllegalArgumentException;
import in.koreatech.koin._common.exception.custom.UnAuthorizedException;
import in.koreatech.koin.domain.user.dto.verification.VerificationCountResponse;
import in.koreatech.koin.domain.user.model.UserDailyVerificationCount;
import in.koreatech.koin.domain.user.model.UserVerificationStatus;
import in.koreatech.koin.domain.user.repository.UserDailyVerificationCountRedisRepository;
import in.koreatech.koin.domain.user.repository.UserVerificationStatusRedisRepository;
import in.koreatech.koin.domain.user.service.verification.VerificationProcessor;
import in.koreatech.koin.domain.user.service.verification.VerificationProcessorFactory;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserVerificationService {

    private final VerificationProcessorFactory verificationProcessorFactory;
    private final UserVerificationStatusRedisRepository userVerificationStatusRedisRepository;
    private final UserDailyVerificationCountRedisRepository userDailyVerificationCountRedisRepository;

    @Transactional
    public void sendCode(String phoneNumberOrEmail) {
        VerificationProcessor verificationProcessor = verificationProcessorFactory.getProcessor(phoneNumberOrEmail);
        increaseUserDailyVerificationCount(phoneNumberOrEmail);
        verificationProcessor.sendCode(phoneNumberOrEmail);
    }

    private void increaseUserDailyVerificationCount(String phoneNumberOrEmail) {
        UserDailyVerificationCount verificationCount = userDailyVerificationCountRedisRepository.findById(
                phoneNumberOrEmail)
            .map(existing -> {
                existing.incrementVerificationCount();
                return existing;
            })
            .orElseGet(() -> UserDailyVerificationCount.from(phoneNumberOrEmail));
        userDailyVerificationCountRedisRepository.save(verificationCount);
    }

    @Transactional
    public void verifyCode(String phoneNumberOrEmail, String verificationCode) {
        UserVerificationStatus verificationStatus = userVerificationStatusRedisRepository.getById(phoneNumberOrEmail);
        if (verificationStatus.isVerified()) {
            return;
        }
        if (!Objects.equals(verificationStatus.getVerificationCode(), verificationCode)) {
            throw new KoinIllegalArgumentException("인증 번호가 일치하지 않습니다.");
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
        VerificationProcessor verificationProcessor = verificationProcessorFactory.getProcessor(phoneNumberOrEmail);
        return verificationProcessor.findId(phoneNumberOrEmail);
    }

    @Transactional
    public void resetPasswordByVerification(String userId, String phoneNumberOrEmail, String newPassword) {
        checkVerified(phoneNumberOrEmail);
        VerificationProcessor verificationProcessor = verificationProcessorFactory.getProcessor(phoneNumberOrEmail);
        verificationProcessor.resetPassword(userId, phoneNumberOrEmail, newPassword);
    }

    private void checkVerified(String phoneNumberOrEmail) {
        userVerificationStatusRedisRepository.findById(phoneNumberOrEmail)
            .filter(UserVerificationStatus::isVerified)
            .orElseThrow(() -> new UnAuthorizedException("본인 인증 후 다시 시도해주십시오."));
        userVerificationStatusRedisRepository.deleteById(phoneNumberOrEmail);
    }
}
