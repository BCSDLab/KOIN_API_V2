package in.koreatech.koin.infrastructure.email.client;

import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.SendEmailRequest;

@Slf4j
@Component
@RequiredArgsConstructor
public class SesMailSender {

    private final SesClient sesClient;

    @Retryable
    public void sendMail(SendEmailRequest request) {
        sesClient.sendEmail(request);
    }

    @Recover
    public void mailRecovery(Exception e, String from, String to, String subject) {
        log.error("메일 전송에 실패했습니다. from: {}, to: {}, subject: {}", from, to, subject, e);
    }
}
