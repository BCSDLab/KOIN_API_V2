package in.koreatech.koin.domain.user.model;

import java.util.concurrent.TimeUnit;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;
import org.springframework.data.redis.core.index.Indexed;

import lombok.Getter;

@Getter
@RedisHash("passwordResetToken")
public class PasswordResetToken {

    private static final int RESET_TOKEN_EXPIRE_HOUR = 1;

    @Id
    private Integer id;

    @Indexed
    private final String resetToken;

    @TimeToLive(unit = TimeUnit.HOURS)
    private int expiration;

    private PasswordResetToken(String resetToken, Integer id) {
        this.resetToken = resetToken;
        this.expiration = RESET_TOKEN_EXPIRE_HOUR;
        this.id = id;
    }

    public static PasswordResetToken from(String resetToken, Integer id) {
        return new PasswordResetToken(resetToken, id);
    }
}
