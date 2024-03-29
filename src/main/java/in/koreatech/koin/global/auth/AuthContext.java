package in.koreatech.koin.global.auth;

import java.util.Objects;

import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import in.koreatech.koin.domain.user.model.UserType;
import in.koreatech.koin.global.auth.exception.AuthenticationException;

@Component
@RequestScope
public class AuthContext {

    private Long userId;

    public Long getUserId() {
        if (userId == null) {
            throw AuthenticationException.withDetail("userId is null");
        }
        return userId;
    }

    public boolean isAnonymous() {
        return Objects.equals(userId, UserType.ANONYMOUS_ID);
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
