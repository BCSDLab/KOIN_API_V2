package in.koreatech.koin.integration.email.form;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Map;

public class OwnerPasswordChangeData implements MailFormData {

    private static final String SUBJECT = "코인 사장님 비밀번호 찾기 이메일 인증";
    private static final String PATH = "owner_change_password_certificate_number";

    private final String email;
    private final String certificationCode;
    private final LocalDateTime now;

    public OwnerPasswordChangeData(String email, String certificationCode, Clock clock) {
        this.email = email;
        this.certificationCode = certificationCode;
        this.now = LocalDateTime.now(clock);
    }

    @Override
    public Map<String, String> getContent() {
        return Map.of(
            "emailAddress", email,
            "certificationCode", certificationCode,
            "year", String.valueOf(now.getYear()),
            "month", String.valueOf(now.getMonthValue()),
            "day", String.valueOf(now.getDayOfMonth()),
            "hour", String.valueOf(now.getHour()),
            "minute", String.valueOf(now.getMinute())
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
