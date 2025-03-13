package in.koreatech.koin._common.auth;

import java.util.Objects;

import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import in.koreatech.koin.domain.user.model.UserType;

@Component
@RequestScope
public class UserIdContext {

    private Integer userId;

    public Integer getUserId() {
        if (Objects.equals(userId, UserType.ANONYMOUS_ID)) {
            return null;
        }
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }
}
