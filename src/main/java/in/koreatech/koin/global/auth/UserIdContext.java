package in.koreatech.koin.global.auth;

import java.util.Objects;

import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import in.koreatech.koin.domain.user.model.UserType;

@Component
@RequestScope
public class UserIdContext {

    private Long userId;

    public Long getUserId() {
        if (Objects.equals(userId, UserType.ANONYMOUS_ID)) {
            return null;
        }
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
