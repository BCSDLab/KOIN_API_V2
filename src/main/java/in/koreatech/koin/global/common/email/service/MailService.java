package in.koreatech.koin.global.common.email.service;

import java.io.StringWriter;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
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

    private SesMailSender sesMailSender;
    private VelocityEngine velocityEngine;

    public CertificationCode sendMail(Email email, MailForm form) {
        CertificationCode certificationCode = RandomGenerator.getCertificationCode();
        String mailForm = mailFormFor(certificationCode, form.getPath());
        sendMailFor(email, mailForm, form.getSubject());
        return certificationCode;
    }

    private void sendMailFor(Email email, String mailForm, String subject) {

    }

    private String mailFormFor(CertificationCode certificationCode, String formLocation) {
        Template template = velocityEngine.getTemplate(formLocation);
        StringWriter writer = new StringWriter();

        Mail mail = Mail.builder()
            .certificationCode(certificationCode.getValue())
            .build();

        VelocityContext context = mail.convertToMap();

        template.merge(context, writer);

        return writer.toString();
    }
}
