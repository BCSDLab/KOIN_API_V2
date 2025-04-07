package in.koreatech.koin.domain.user.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

import in.koreatech.koin.domain.user.exception.UserVerificationDailyLimitExceededException;
import lombok.Getter;

@Getter
@RedisHash(value = "smsDailyVerificationCount")
public class SmsDailyVerificationCount {

    private static final long EXPIRATION_SECONDS = 60 * 60 * 24L;
    private static final int MAX_VERIFICATION_COUNT = 5;

    @Id
    private String phoneNumber;

    private int verificationCount = 1;

    @TimeToLive
    private Long expiration;

    public SmsDailyVerificationCount(String phoneNumber) {
        this.phoneNumber = phoneNumber;
        this.expiration = EXPIRATION_SECONDS;
    }

    public static SmsDailyVerificationCount from(String phoneNumber) {
        return new SmsDailyVerificationCount(phoneNumber);
    }

    public void incrementVerificationCount() {
        if (verificationCount >= MAX_VERIFICATION_COUNT) {
            throw UserVerificationDailyLimitExceededException.withDetail("limit: " + MAX_VERIFICATION_COUNT);
        }
        verificationCount++;
    }
}
