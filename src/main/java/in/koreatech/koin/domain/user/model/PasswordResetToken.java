package in.koreatech.koin.domain.user.model;

import java.util.concurrent.TimeUnit;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

import lombok.Getter;

@Getter
@RedisHash("resetToken")
public class PasswordResetToken {

    private static final int RESET_TOKEN_EXPIRE_HOUR = 1;

    @Id
    private Integer id;

    private final String resetToken;

    @TimeToLive(unit = TimeUnit.HOURS)
    private final int expiration;

    private PasswordResetToken(String resetToken, Integer id) {
        this.resetToken = resetToken;
        this.expiration = RESET_TOKEN_EXPIRE_HOUR;
        this.id = id;
    }

    public static PasswordResetToken from(String resetToken, Integer id) {
        return new PasswordResetToken(resetToken, id);
    }
}
