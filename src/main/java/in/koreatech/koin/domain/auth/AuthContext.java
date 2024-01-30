package in.koreatech.koin.domain.auth;

import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import in.koreatech.koin.domain.auth.exception.AuthException;

@Component
@RequestScope
public class AuthContext {

    private Long userId;

    public boolean unAuthenticated() {
        return userId == null;
    }

    public Long getUserId() {
        if (unAuthenticated()) {
            throw new AuthException("인증되지 않은 사용자입니다.");
        }
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
