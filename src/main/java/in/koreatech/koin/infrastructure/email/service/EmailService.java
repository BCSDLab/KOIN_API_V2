package in.koreatech.koin.infrastructure.email.service;

import java.util.Map;

import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import in.koreatech.koin.infrastructure.email.form.EmailForm;
import in.koreatech.koin.infrastructure.email.client.SesMailSender;
import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.services.ses.model.SendEmailRequest;

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

        return SendEmailRequest.builder()
            .destination(destination -> destination.toAddresses(targetEmail))
            .source(NO_REPLY_EMAIL_ADDRESS)
            .message(message -> message
                .body(body -> body.html(content -> content.charset("UTF-8").data(htmlBody)))
                .subject(content -> content.charset("UTF-8").data(emailForm.getSubject())))
            .build();
    }
}
