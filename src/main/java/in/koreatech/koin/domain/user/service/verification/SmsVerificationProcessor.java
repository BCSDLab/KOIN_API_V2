package in.koreatech.koin.domain.user.service.verification;

import java.util.List;
import java.util.Objects;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin._common.event.UserSmsRequestEvent;
import in.koreatech.koin._common.exception.custom.KoinIllegalArgumentException;
import in.koreatech.koin._common.util.random.CertificateNumberGenerator;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.model.UserType;
import in.koreatech.koin.domain.user.model.UserVerificationStatus;
import in.koreatech.koin.domain.user.model.VerificationType;
import in.koreatech.koin.domain.user.repository.UserRepository;
import in.koreatech.koin.domain.user.repository.UserVerificationStatusRedisRepository;
import in.koreatech.koin.integration.naver.service.NaverSmsService;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SmsVerificationProcessor implements VerificationProcessor {

    private static final VerificationType VERIFICATION_TYPE = VerificationType.SMS;
    private static final long INITIAL_EXPIRATION_SECONDS = 60 * 3L;

    private final UserVerificationStatusRedisRepository userVerificationStatusRedisRepository;
    private final NaverSmsService naverSmsService;
    private final ApplicationEventPublisher eventPublisher;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public VerificationType getType() {
        return VERIFICATION_TYPE;
    }

    @Transactional
    @Override
    public void sendCode(String phoneNumber) {
        String verificationCode = CertificateNumberGenerator.generate();
        naverSmsService.sendVerificationCode(verificationCode, phoneNumber);
        userVerificationStatusRedisRepository.save(
            UserVerificationStatus.of(phoneNumber, verificationCode, INITIAL_EXPIRATION_SECONDS));
        eventPublisher.publishEvent(new UserSmsRequestEvent(phoneNumber));
    }

    @Transactional
    @Override
    public String findId(String phoneNumber) {
        User user = userRepository.getByPhoneNumberAndUserTypeIn(phoneNumber,
            List.of(UserType.GENERAL, UserType.STUDENT));
        return user.getUserId();
    }

    @Transactional
    @Override
    public void resetPassword(String userId, String phoneNumber, String newPassword) {
        User user = userRepository.getByUserIdAndUserTypeIn(userId, List.of(UserType.GENERAL, UserType.STUDENT));
        if (!Objects.equals(user.getPhoneNumber(), phoneNumber)) {
            throw new KoinIllegalArgumentException("입력한 아이디와 인증된 사용자 정보가 일치하지 않습니다.");
        }
        user.updatePassword(passwordEncoder, newPassword);
        userRepository.save(user);
    }
}
