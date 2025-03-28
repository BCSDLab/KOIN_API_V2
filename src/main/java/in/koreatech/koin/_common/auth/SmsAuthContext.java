package in.koreatech.koin._common.auth;

import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import lombok.Getter;
import lombok.Setter;

@Component
@RequestScope
@Setter
@Getter
public class SmsAuthContext {

    private String phoneNumberAuthed;
}
