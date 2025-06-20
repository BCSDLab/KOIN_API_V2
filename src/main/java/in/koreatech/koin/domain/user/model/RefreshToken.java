package in.koreatech.koin.domain.user.model;

import java.util.concurrent.TimeUnit;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

import lombok.Getter;

@Getter
@RedisHash(value = "refreshToken")
public class RefreshToken {

    private static final long REFRESH_TOKEN_EXPIRE_DAY = 90L;
    private static final String REFRESH_TOKEN_KEY_FORMAT = "%d:%s";
    private static final String REFRESH_TOKEN_FORMAT = "%s-%d";

    @Id
    private String id;

    private String token;

    @TimeToLive(unit = TimeUnit.DAYS)
    private Long expiration;

    private RefreshToken(String id, String token, Long expiration) {
        this.id = id;
        this.token = token;
        this.expiration = expiration;
    }

    public static RefreshToken create(Integer userId, String refreshToken, String platform) {
        return new RefreshToken(
            String.format(REFRESH_TOKEN_KEY_FORMAT, userId, platform),
            String.format(REFRESH_TOKEN_FORMAT, refreshToken, userId),
            REFRESH_TOKEN_EXPIRE_DAY
        );
    }

    public static String generateKey(Integer userId, String platform) {
        return String.format(REFRESH_TOKEN_KEY_FORMAT, userId, platform);
    }
}
