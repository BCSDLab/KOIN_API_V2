package in.koreatech.koin._common.auth;

import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import in.koreatech.koin._common.auth.exception.AuthenticationException;

@Component
@RequestScope
public class SmsAuthedContext {

    private String phoneNumberAuthed;

    public String getPhoneNumberAuthed() {
        if (phoneNumberAuthed == null) {
            throw AuthenticationException.withDetail("미인증된 휴대폰 번호입니다.");
        }
        return phoneNumberAuthed;
    }

    public void setPhoneNumberAuthed(String phoneNumberAuthed) {
        this.phoneNumberAuthed = phoneNumberAuthed;
    }
}
