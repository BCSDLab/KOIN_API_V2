package in.koreatech.koin.infrastructure.email.form;

import java.util.Map;

public class StudentRegisterRequestEmailForm implements EmailForm {

    private static final String SUBJECT = "학교 이메일 주소 인증";
    private static final String PATH = "student_register_certificate_number";

    private final String contextPath;
    private final String authToken;

    public StudentRegisterRequestEmailForm(String contextPath, String authToken) {
        this.contextPath = contextPath;
        this.authToken = authToken;
    }

    @Override
    public Map<String, String> getContent() {
        return Map.of(
            "contextPath", contextPath,
            "authToken", authToken
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
