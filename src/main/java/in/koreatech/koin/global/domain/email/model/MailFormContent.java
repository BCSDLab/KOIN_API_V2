package in.koreatech.koin.global.domain.email.model;

public enum MailFormContent {
    NO_REPLY_EMAIL_ADDRESS("no-reply@bcsdlab.com");

    private final String content;

    MailFormContent(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }
}
