package in.koreatech.koin.infrastructure.email.service;

import java.util.Map;

import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import in.koreatech.koin.infrastructure.email.form.EmailForm;
import in.koreatech.koin.infrastructure.email.model.SesMailSender;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailService {

    private static final String NO_REPLY_EMAIL_ADDRESS = "no-reply@bcsdlab.com";

    private final SesMailSender sesMailSender;
    private final TemplateEngine templateEngine;

    public void sendVerificationEmail(String targetEmail, EmailForm emailForm) {
        String mailForm = generateMailForm(emailForm.getContent(), emailForm.getFilePath());
        String subject = emailForm.getSubject();
        sesMailSender.sendMail(NO_REPLY_EMAIL_ADDRESS, targetEmail, subject, mailForm);
    }

    private String generateMailForm(Map<String, String> contents, String fileLocation) {
        Context context = new Context();
        contents.forEach(context::setVariable);
        return templateEngine.process(fileLocation, context);
    }
}
