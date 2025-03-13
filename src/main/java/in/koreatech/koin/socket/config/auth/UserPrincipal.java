package in.koreatech.koin.socket.config.auth;

import java.security.Principal;

import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.model.UserType;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserPrincipal implements Principal {

    private Integer userId;
    private String userName;
    private UserType userType;
    private String deviceToken;

    @Override
    public String getName() {
        return userId.toString();
    }

    public static UserPrincipal of(User user) {
        return new UserPrincipal(user.getId(), user.getName(), user.getUserType(), user.getDeviceToken());
    }
}
