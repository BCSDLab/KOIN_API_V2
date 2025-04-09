package in.koreatech.koin.domain.user.service.verification;

import org.springframework.transaction.annotation.Transactional;

public interface VerificationProcessor {

    @Transactional
    void sendCode(String phoneNumberOrEmail);

    @Transactional
    String findId(String phoneNumberOrEmail);

    @Transactional
    void resetPassword(String userId, String phoneNumberOrEmail, String newPassword);
}
