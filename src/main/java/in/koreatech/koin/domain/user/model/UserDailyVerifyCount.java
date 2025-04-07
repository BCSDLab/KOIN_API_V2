package in.koreatech.koin.domain.user.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

import in.koreatech.koin.domain.user.exception.UserVerificationDailyLimitExceededException;
import lombok.Getter;

@Getter
@RedisHash(value = "userDailyVerifyCount")
public class UserDailyVerifyCount {

    private static final long EXPIRATION_SECONDS = 60 * 60 * 24L;
    private static final int MAX_VERIFICATION_COUNT = 5;

    @Id
    private String id;

    private int verificationCount = 1;

    @TimeToLive
    private Long expiration;

    public UserDailyVerifyCount(String id) {
        this.id = id;
        this.expiration = EXPIRATION_SECONDS;
    }

    public static UserDailyVerifyCount from(String id) {
        return new UserDailyVerifyCount(id);
    }

    public void incrementVerificationCount() {
        if (verificationCount >= MAX_VERIFICATION_COUNT) {
            throw UserVerificationDailyLimitExceededException.withDetail("limit: " + MAX_VERIFICATION_COUNT);
        }
        verificationCount++;
    }
}
