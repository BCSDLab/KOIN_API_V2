package in.koreatech.koin.domain.user.service.verification;

import org.springframework.transaction.annotation.Transactional;

public interface VerificationProcessor {

    @Transactional
    void sendCode(String receiver);

    @Transactional
    String findId(String target);

    @Transactional
    void resetPassword(String userId, String target, String newPassword);
}
