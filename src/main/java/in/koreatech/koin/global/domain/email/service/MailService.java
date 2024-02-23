package in.koreatech.koin.global.domain.email.service;

import static in.koreatech.koin.global.domain.email.model.MailFormContent.NO_REPLY_EMAIL_ADDRESS;

import java.io.StringWriter;
import java.util.Properties;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.springframework.stereotype.Service;

import in.koreatech.koin.global.domain.email.model.CertificationCode;
import in.koreatech.koin.global.domain.email.model.Email;
import in.koreatech.koin.global.domain.email.model.Mail;
import in.koreatech.koin.global.domain.email.model.MailForm;
import in.koreatech.koin.global.domain.email.model.SesMailSender;
import in.koreatech.koin.global.domain.random.model.RandomCertificationNumber;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MailService {

    private final SesMailSender sesMailSender;

    public CertificationCode sendMail(Email email, MailForm form) {
        CertificationCode certificationCode = RandomCertificationNumber.getCertificationCode();
        mailFormLoaderInit();
        String mailForm = mailFormFor(certificationCode, form.getPath());
        sendMailFor(email, mailForm, form.getSubject());
        return certificationCode;
    }

    private void mailFormLoaderInit() {
        Properties properties = new Properties();
        properties.setProperty("resource.loader", "class");
        properties.setProperty("class.resource.loader.class",
            "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        Velocity.init(properties);
    }

    private void sendMailFor(Email email, String mailForm, String purpose) {
        sesMailSender.sendMail(NO_REPLY_EMAIL_ADDRESS.getContent(), email.getEmail(), purpose, mailForm);
    }

    private String mailFormFor(CertificationCode certificationCode, String formLocation) {
        Template template = Velocity.getTemplate(formLocation);

        Mail mail = Mail.builder()
            .certificationCode(certificationCode.getValue())
            .build();
        VelocityContext context = mail.convertToMap();

        StringWriter writer = new StringWriter();
        template.merge(context, writer);

        return writer.toString();
    }
}
