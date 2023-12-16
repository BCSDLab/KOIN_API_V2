package in.koreatech.koin.domain.user;

import java.util.concurrent.TimeUnit;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

@Getter
@RedisHash("refreshToken")
public class UserToken {

    @Id
    private Long id;

    private final String refreshToken;

    @TimeToLive(unit = TimeUnit.DAYS)
    private final Long expiration;

    private UserToken(Long id, String refreshToken, Long expiration) {
        this.id = id;
        this.refreshToken = refreshToken;
        this.expiration = expiration;

    }

    public static UserToken create(Long userId, String refreshToken) {
        return new UserToken(userId, refreshToken, 3L);
    }
}
