package in.koreatech.koin.domain.user.service.verification;

import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.user.model.VerificationType;

public interface VerificationProcessor {

    VerificationType getType();

    @Transactional
    void sendCode(String phoneNumberOrEmail);

    @Transactional
    String findId(String phoneNumberOrEmail);

    @Transactional
    void resetPassword(String userId, String phoneNumberOrEmail, String newPassword);
}
