package in.koreatech.koin.global.domain.email.model;

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

    private final AmazonSimpleEmailServiceAsync amazonSimpleEmailServiceAsync;

    public void sendMail(String from, String to, String subject, String htmlBody) {
        SendEmailRequest request = new SendEmailRequest()
            .withDestination(new Destination().withToAddresses(to))
            .withSource(from)
            .withMessage(new Message()
                .withBody(new Body().withHtml(new Content().withCharset("UTF-8").withData(htmlBody)))
                .withSubject(new Content().withCharset("UTF-8").withData(subject)));

        amazonSimpleEmailServiceAsync.sendEmailAsync(request, new AsyncHandler<>() {
            @Override
            public void onError(Exception e) {
                log.warn(e.getMessage());
            }

            @Override
            public void onSuccess(SendEmailRequest request, SendEmailResult sendEmailResult) {
                log.info("Email sent successfully");
            }
        });
    }
}
