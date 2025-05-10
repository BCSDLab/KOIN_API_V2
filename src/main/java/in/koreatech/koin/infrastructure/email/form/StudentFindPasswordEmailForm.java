package in.koreatech.koin.infrastructure.email.form;

import java.util.Map;

public class StudentFindPasswordEmailForm implements EmailForm {
    private static final String SUBJECT = "코인 패스워드 초기화 인증";
    private static final String PATH = "student_change_password_certificate_button";

    private final String contextPath;
    private final String resetToken;

    public StudentFindPasswordEmailForm(String contextPath, String resetToken) {
        this.contextPath = contextPath;
        this.resetToken = resetToken;
    }

    @Override
    public Map<String, String> getContent() {
        return Map.of(
            "contextPath", contextPath,
            "resetToken", resetToken
        );
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
