package in.koreatech.koin.infrastructure.email.service;

import java.util.Map;

import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.amazonaws.services.simpleemail.model.Body;
import com.amazonaws.services.simpleemail.model.Content;
import com.amazonaws.services.simpleemail.model.Destination;
import com.amazonaws.services.simpleemail.model.Message;
import com.amazonaws.services.simpleemail.model.SendEmailRequest;

import in.koreatech.koin.infrastructure.email.form.EmailForm;
import in.koreatech.koin.infrastructure.email.client.SesMailSender;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailService {

    private static final String NO_REPLY_EMAIL_ADDRESS = "no-reply@bcsdlab.com";

    private final SesMailSender sesMailSender;
    private final TemplateEngine templateEngine;

    public void sendVerificationEmail(String targetEmail, EmailForm emailForm) {
        SendEmailRequest request = createEmailRequest(targetEmail, emailForm);
        sesMailSender.sendMail(request);
    }

    private SendEmailRequest createEmailRequest(String targetEmail, EmailForm emailForm) {
        Context context = new Context();
        Map<String, String> contents = emailForm.getContent();
        contents.forEach(context::setVariable);

        String htmlBody = templateEngine.process(emailForm.getFilePath(), context);

        return new SendEmailRequest()
            .withDestination(new Destination().withToAddresses(targetEmail))
            .withSource(NO_REPLY_EMAIL_ADDRESS)
            .withMessage(new Message()
                .withBody(new Body().withHtml(new Content().withCharset("UTF-8").withData(htmlBody)))
                .withSubject(new Content().withCharset("UTF-8").withData(emailForm.getSubject())));
    }
}
