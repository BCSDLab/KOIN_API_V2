package in.koreatech.koin.infrastructure.email.client;

import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.model.Body;
import com.amazonaws.services.simpleemail.model.Content;
import com.amazonaws.services.simpleemail.model.Destination;
import com.amazonaws.services.simpleemail.model.Message;
import com.amazonaws.services.simpleemail.model.SendEmailRequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class SesMailSender {

    private final AmazonSimpleEmailService amazonSimpleEmailService;

    @Retryable
    public void sendMail(SendEmailRequest request) {
        amazonSimpleEmailService.sendEmail(request);
    }

    @Recover
    public void mailRecovery(Exception e, String from, String to, String subject) {
        log.error("메일 전송에 실패했습니다. from: {}, to: {}, subject: {}", from, to, subject, e);
    }
}
