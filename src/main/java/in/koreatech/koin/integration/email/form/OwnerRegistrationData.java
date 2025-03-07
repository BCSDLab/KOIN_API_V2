package in.koreatech.koin.integration.email.form;

import java.util.Map;

public class OwnerRegistrationData implements MailFormData {

    private static final String SUBJECT = "코인 사장님 회원가입 이메일 인증";
    private static final String PATH = "owner_register_certificate_number";

    private final String certificationCode;

    public OwnerRegistrationData(String certificationCode) {
        this.certificationCode = certificationCode;
    }

    @Override
    public Map<String, String> getContent() {
        return Map.of("certificationCode", certificationCode);
    }

    @Override
    public String getSubject() {
        return SUBJECT;
    }

    @Override
    public String getFilePath() {
        return PATH;
    }
}
