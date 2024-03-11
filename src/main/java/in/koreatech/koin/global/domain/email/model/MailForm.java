package in.koreatech.koin.global.domain.email.model;

public enum MailForm {

    OWNER_REGISTRATION_MAIL_FORM("코인 사장님 회원가입 이메일 인증",
        "owner_register_certificate_number"),
    ;

    private final String subject;
    private final String path;

    MailForm(String subject, String path) {
        this.subject = subject;
        this.path = path;
    }

    public String getSubject() {
        return subject;
    }

    public String getPath() {
        return path;
    }
}
