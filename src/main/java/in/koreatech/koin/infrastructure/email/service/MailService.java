package in.koreatech.koin.infrastructure.email.service;

import java.util.Map;

import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import in.koreatech.koin._common.model.MailFormData;
import in.koreatech.koin.infrastructure.email.model.SesMailSender;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MailService {

    private static final String NO_REPLY_EMAIL_ADDRESS = "no-reply@bcsdlab.com";

    private final SesMailSender sesMailSender;
    private final TemplateEngine templateEngine;

    public void sendMail(String targetEmail, MailFormData mailFormData) {
        String mailForm = generateMailForm(mailFormData.getContent(), mailFormData.getFilePath());
        String subject = mailFormData.getSubject();
        sesMailSender.sendMail(NO_REPLY_EMAIL_ADDRESS, targetEmail, subject, mailForm);
    }

    private String generateMailForm(Map<String, String> contents, String fileLocation) {
        Context context = new Context();
        contents.forEach(context::setVariable);
        return templateEngine.process(fileLocation, context);
    }
}
