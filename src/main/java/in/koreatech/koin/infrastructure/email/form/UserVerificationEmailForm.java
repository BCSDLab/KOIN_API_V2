package in.koreatech.koin.infrastructure.email.form;

import java.util.Map;

public class UserVerificationEmailForm implements EmailForm {

    private static final String SUBJECT = "코인 이메일 인증";
    private static final String PATH = "user_email_verification";

    private final String verificationCode;

    public UserVerificationEmailForm(String verificationCode) {
        this.verificationCode = verificationCode;
    }

    @Override
    public Map<String, String> getContent() {
        return Map.of("code", verificationCode);
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
