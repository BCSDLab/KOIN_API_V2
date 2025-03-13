package in.koreatech.koin.integration.email.model;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Component;

import com.amazonaws.handlers.AsyncHandler;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceAsync;
import com.amazonaws.services.simpleemail.model.Body;
import com.amazonaws.services.simpleemail.model.Content;
import com.amazonaws.services.simpleemail.model.Destination;
import com.amazonaws.services.simpleemail.model.Message;
import com.amazonaws.services.simpleemail.model.SendEmailRequest;
import com.amazonaws.services.simpleemail.model.SendEmailResult;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class SesMailSender {

    private static final int maxRetries = 3;
    private static final long retryInterval = 500L;

    private final AmazonSimpleEmailServiceAsync amazonSimpleEmailServiceAsync;
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public void sendMail(String from, String to, String subject, String htmlBody) {
        SendEmailRequest request = new SendEmailRequest()
            .withDestination(new Destination().withToAddresses(to))
            .withSource(from)
            .withMessage(new Message()
                .withBody(new Body().withHtml(new Content().withCharset("UTF-8").withData(htmlBody)))
                .withSubject(new Content().withCharset("UTF-8").withData(subject)));

        sendEmailWithRetry(request, 0);
    }

    private void sendEmailWithRetry(SendEmailRequest request, int retryCount) {
        amazonSimpleEmailServiceAsync.sendEmailAsync(request, new AsyncHandler<>() {
            @Override
            public void onError(Exception e) {
                log.warn(e.getMessage());

                if (retryCount < maxRetries) {
                    scheduler.schedule(() -> sendEmailWithRetry(request, retryCount + 1), retryInterval, TimeUnit.MILLISECONDS);
                } else {
                    log.error("메일 전송 재시도 횟수의 최대치를 초과했습니다.");
                }
            }

            @Override
            public void onSuccess(SendEmailRequest request, SendEmailResult sendEmailResult) {
                log.info("메일이 성공적으로 전송됐습니다.");
            }
        });
    }
}
