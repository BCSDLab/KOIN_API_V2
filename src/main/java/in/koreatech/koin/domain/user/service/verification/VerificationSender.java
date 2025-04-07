package in.koreatech.koin.domain.user.service.verification;

import org.springframework.transaction.annotation.Transactional;

public interface VerificationSender {

    @Transactional
    void sendCode(String receiver);
}
