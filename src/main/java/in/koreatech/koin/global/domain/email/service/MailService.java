package in.koreatech.koin.global.domain.email.service;

import static in.koreatech.koin.global.domain.email.model.MailFormContent.NO_REPLY_EMAIL_ADDRESS;

import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import in.koreatech.koin.global.domain.email.model.CertificationCode;
import in.koreatech.koin.global.domain.email.model.EmailAddress;
import in.koreatech.koin.global.domain.email.model.Mail;
import in.koreatech.koin.global.domain.email.model.MailForm;
import in.koreatech.koin.global.domain.email.model.SesMailSender;
import in.koreatech.koin.global.domain.random.model.RandomCertificationNumber;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MailService {

    private final SesMailSender sesMailSender;

    private final TemplateEngine templateEngine;

    public CertificationCode sendMail(EmailAddress email, MailForm form) {
        CertificationCode certificationCode = RandomCertificationNumber.getCertificationCode();
        String mailForm = mailFormFor(certificationCode, form.getPath());
        sendMailFor(email, mailForm, form.getSubject());
        return certificationCode;
    }

    private void sendMailFor(EmailAddress email, String mailForm, String purpose) {
        sesMailSender.sendMail(NO_REPLY_EMAIL_ADDRESS.getContent(), email.email(), purpose, mailForm);
    }

    private String mailFormFor(CertificationCode certificationCode, String formLocation) {
        Mail mail = Mail.builder()
            .certificationCode(certificationCode.getValue())
            .build();
        Context context = mail.convertToMap();

        return templateEngine.process(formLocation, context);
    }
}
