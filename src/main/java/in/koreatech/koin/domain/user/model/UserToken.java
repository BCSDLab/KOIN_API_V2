package in.koreatech.koin.domain.user.model;

import java.util.concurrent.TimeUnit;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

import lombok.Getter;

@Getter
@RedisHash("refreshToken")
public class UserToken {

    private static final long REFRESH_TOKEN_EXPIRE_DAY = 90L;

    @Id
    private Integer id;

    private final String refreshToken;

    @TimeToLive(unit = TimeUnit.DAYS)
    private final Long expiration;

    private UserToken(Integer id, String refreshToken, Long expiration) {
        this.id = id;
        this.refreshToken = refreshToken;
        this.expiration = expiration;
    }

    public static UserToken create(Integer userId, String refreshToken) {
        return new UserToken(userId, refreshToken, REFRESH_TOKEN_EXPIRE_DAY);
    }
}
