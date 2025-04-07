package in.koreatech.koin.domain.user.service.verification;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin._common.util.random.CertificateNumberGenerator;
import in.koreatech.koin.domain.user.model.UserVerificationStatus;
import in.koreatech.koin.domain.user.repository.UserVerificationStatusRedisRepository;
import in.koreatech.koin.integration.email.form.MailFormData;
import in.koreatech.koin.integration.email.form.UserEmailVerificationData;
import in.koreatech.koin.integration.email.service.MailService;
import lombok.RequiredArgsConstructor;

@Component("email")
@RequiredArgsConstructor
public class EmailVerificationSender implements VerificationSender {

    private static final long INITIAL_EXPIRATION_SECONDS = 60 * 5L;

    private final MailService mailService;
    private final UserVerificationStatusRedisRepository userVerificationStatusRedisRepository;

    @Transactional
    @Override
    public void sendCode(String email) {
        // 인증번호 생성
        String verificationCode = CertificateNumberGenerator.generate();

        // 이메일 전송
        MailFormData mailFormData = new UserEmailVerificationData(verificationCode);
        mailService.sendMail(email, mailFormData);

        // 인증 정보 저장
        userVerificationStatusRedisRepository.save(
            UserVerificationStatus.of(email, verificationCode, INITIAL_EXPIRATION_SECONDS));
    }
}
