package in.koreatech.koin.domain.user.model;

import java.time.Clock;
import java.time.LocalDateTime;
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

    private PasswordResetToken(String resetToken, int expiration, Integer id) {
        this.resetToken = resetToken;
        this.expiration = expiration;
        this.id = id;
    }

    public static PasswordResetToken from(Clock clock, Integer userId, String email) {
        return new PasswordResetToken(
            email + LocalDateTime.now(clock).plusHours(RESET_TOKEN_EXPIRE_HOUR),
            RESET_TOKEN_EXPIRE_HOUR,
            userId
        );
    }
}
