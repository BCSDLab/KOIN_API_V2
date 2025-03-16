package in.koreatech.koin.domain.user.model;

import java.util.concurrent.TimeUnit;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@RedisHash(value = "refreshToken")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshToken {

    private static final long REFRESH_TOKEN_EXPIRE_DAY = 90L;
    private static final String REFRESH_TOKEN_KEY_FORMAT = "%d:%s";

    @Id
    private String id;

    private String token;

    @TimeToLive(unit = TimeUnit.DAYS)
    private Long expiration;

    private RefreshToken(Integer userId, String refreshToken, String platform) {
        this.id = generateKey(userId, platform);
        this.token = refreshToken + "-" + userId;
        this.expiration = REFRESH_TOKEN_EXPIRE_DAY;
    }

    public static RefreshToken create(Integer userId, String refreshToken, String platform) {
        return new RefreshToken(userId, refreshToken, platform);
    }

    public static String generateKey(Integer userId, String platform) {
        return String.format(REFRESH_TOKEN_KEY_FORMAT, userId, platform);
    }
}
