package in.koreatech.koin.domain.user.verification.service;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin._common.event.UserEmailVerificationSendEvent;
import in.koreatech.koin._common.event.UserSmsVerificationSendEvent;
import in.koreatech.koin._common.util.random.VerificationNumberGenerator;
import in.koreatech.koin.domain.user.verification.dto.SendVerificationResponse;
import in.koreatech.koin.domain.user.verification.model.UserDailyVerificationCount;
import in.koreatech.koin.domain.user.verification.model.UserVerificationStatus;
import in.koreatech.koin.domain.user.verification.repository.UserDailyVerificationCountRedisRepository;
import in.koreatech.koin.domain.user.verification.repository.UserVerificationStatusRedisRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserVerificationService {

    private final UserVerificationStatusRedisRepository userVerificationStatusRedisRepository;
    private final UserDailyVerificationCountRedisRepository userDailyVerificationCountRedisRepository;
    private final VerificationNumberGenerator verificationNumberGenerator;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public SendVerificationResponse sendSmsVerification(String phoneNumber) {
        UserDailyVerificationCount verificationCount = increaseAndGetUserDailyVerificationCount(phoneNumber);
        var userVerificationStatus = UserVerificationStatus.createBySms(phoneNumber, verificationNumberGenerator);
        userVerificationStatusRedisRepository.save(userVerificationStatus);
        eventPublisher.publishEvent(new UserSmsVerificationSendEvent(userVerificationStatus.getVerificationCode(), phoneNumber));
        return SendVerificationResponse.from(verificationCount);
    }

    @Transactional
    public SendVerificationResponse sendEmailVerification(String email) {
        UserDailyVerificationCount verificationCount = increaseAndGetUserDailyVerificationCount(email);
        var userVerificationStatus = UserVerificationStatus.createByEmail(email, verificationNumberGenerator);
        userVerificationStatusRedisRepository.save(userVerificationStatus);
        eventPublisher.publishEvent(new UserEmailVerificationSendEvent(userVerificationStatus.getVerificationCode(), email));
        return SendVerificationResponse.from(verificationCount);
    }

    @Transactional
    public void verifyCode(String phoneNumberOrEmail, String verificationCode) {
        UserVerificationStatus verificationStatus = userVerificationStatusRedisRepository.getById(phoneNumberOrEmail);
        verificationStatus.verify(verificationCode);
        userVerificationStatusRedisRepository.save(verificationStatus);
    }

    /**
     * <p>
     * 이 메서드는 지정된 전화번호 또는 이메일에 대한 본인 인증 상태가
     * <strong>완료되었는지 확인한 후</strong> 인증 정보를 레디스에서 삭제합니다.
     * </p>
     *
     * <p><b>주의:</b> 인증 정보는 이 메서드 호출 시 삭제되므로,
     * 인증이 필요한 비즈니스 로직이 모두 <strong>완료된 후 마지막에</strong> 호출해야 합니다.
     * 인증 상태를 1회성으로 "소비"하는 방식이며, 이후 동일한 인증 상태를 재사용할 수 없습니다.
     * 레디스는 트랜잭션을 지원하지 않으므로 메서드 내에서 오류 발생 시 롤백되지않습니다.
     * </p>
     */
    @Transactional
    public void consumeVerification(String phoneNumberOrEmail) {
        UserVerificationStatus verificationStatus = userVerificationStatusRedisRepository.getById(phoneNumberOrEmail);
        verificationStatus.requireVerified();
        userVerificationStatusRedisRepository.deleteById(phoneNumberOrEmail);
    }

    private UserDailyVerificationCount increaseAndGetUserDailyVerificationCount(String phoneNumberOrEmail) {
        UserDailyVerificationCount count = userDailyVerificationCountRedisRepository.findById(phoneNumberOrEmail)
            .orElseGet(() -> UserDailyVerificationCount.create(phoneNumberOrEmail));
        count.increment();
        return userDailyVerificationCountRedisRepository.save(count);
    }
}
