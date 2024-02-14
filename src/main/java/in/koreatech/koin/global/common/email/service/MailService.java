package in.koreatech.koin.global.common.email.service;

import static in.koreatech.koin.global.common.email.model.MailFormContent.NO_REPLY_EMAIL_ADDRESS;

import java.io.StringWriter;
import java.util.Properties;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.springframework.stereotype.Service;

import in.koreatech.koin.global.common.RandomGenerator;
import in.koreatech.koin.global.common.email.model.CertificationCode;
import in.koreatech.koin.global.common.email.model.Email;
import in.koreatech.koin.global.common.email.model.Mail;
import in.koreatech.koin.global.common.email.model.MailForm;
import in.koreatech.koin.global.common.email.model.SesMailSender;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MailService {

    private final SesMailSender sesMailSender;

    public CertificationCode sendMail(Email email, MailForm form) {
        CertificationCode certificationCode = RandomGenerator.getCertificationCode();
        String mailForm = mailFormFor(certificationCode, form.getPath());
        sendMailFor(email, mailForm, form.getSubject());
        return certificationCode;
    }

    private void sendMailFor(Email email, String mailForm, String purpose) {
        sesMailSender.sendMail(NO_REPLY_EMAIL_ADDRESS.getContent(), email.getEmail(), purpose, mailForm);
    }

    private String mailFormFor(CertificationCode certificationCode, String formLocation) {
        Properties p = new Properties();
        p.setProperty("resource.loader", "class");
        p.setProperty("class.resource.loader.class",
            "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        Velocity.init(p);

        Template template = Velocity.getTemplate(formLocation);
        StringWriter writer = new StringWriter();

        Mail mail = Mail.builder()
            .certificationCode(certificationCode.getValue())
            .build();

        VelocityContext context = mail.convertToMap();

        template.merge(context, writer);

        return writer.toString();
    }
}
