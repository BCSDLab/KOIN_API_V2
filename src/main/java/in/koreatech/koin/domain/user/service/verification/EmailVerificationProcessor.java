package in.koreatech.koin.domain.user.service.verification;

import java.util.List;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin._common.event.UserSmsVerificationSendEvent;
import in.koreatech.koin._common.util.random.CertificateNumberGenerator;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.model.UserType;
import in.koreatech.koin.domain.user.model.UserVerificationStatus;
import in.koreatech.koin.domain.user.model.VerificationType;
import in.koreatech.koin.domain.user.repository.UserRepository;
import in.koreatech.koin.domain.user.repository.UserVerificationStatusRedisRepository;
import in.koreatech.koin.integration.email.form.MailFormData;
import in.koreatech.koin.integration.email.form.UserEmailVerificationData;
import in.koreatech.koin.integration.email.service.MailService;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class EmailVerificationProcessor implements VerificationProcessor {

    private static final VerificationType VERIFICATION_TYPE = VerificationType.EMAIL;
    private static final long INITIAL_EXPIRATION_SECONDS = 60 * 5L;

    private final MailService mailService;
    private final UserVerificationStatusRedisRepository userVerificationStatusRedisRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public VerificationType getType() {
        return VERIFICATION_TYPE;
    }

    @Transactional
    @Override
    public void sendCode(String email) {
        String verificationCode = CertificateNumberGenerator.generate();
        MailFormData mailFormData = new UserEmailVerificationData(verificationCode);
        mailService.sendMail(email, mailFormData);
        userVerificationStatusRedisRepository.save(
            UserVerificationStatus.of(email, verificationCode, INITIAL_EXPIRATION_SECONDS));
        eventPublisher.publishEvent(new UserSmsVerificationSendEvent(email));
    }

    @Transactional
    @Override
    public String findId(String email) {
        User user = userRepository.getByEmailAndUserTypeIn(email, List.of(UserType.GENERAL, UserType.STUDENT));
        return user.getUserId();
    }

    @Transactional
    @Override
    public void resetPassword(String userId, String email, String newPassword) {
        User user = userRepository.getByEmailAndUserTypeIn(email, List.of(UserType.GENERAL, UserType.STUDENT));
        user.updatePassword(passwordEncoder, newPassword);
        userRepository.save(user);
    }
}
