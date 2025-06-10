package in.koreatech.koin.domain.user.verification.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

import in.koreatech.koin._common.exception.custom.TooManyRequestsException;
import lombok.Getter;

@Getter
@RedisHash(value = "userDailyVerificationCount")
public class UserDailyVerificationCount {

    private static final long VERIFIED_EXPIRATION_SECONDS = 60 * 60 * 24L; // 24시간
    public static final int MAX_VERIFICATION_COUNT = 5;

    @Id
    private String id;

    private int verificationCount = 1;

    @TimeToLive
    private final Long expiration;

    private UserDailyVerificationCount(String id) {
        this.id = id;
        this.expiration = VERIFIED_EXPIRATION_SECONDS;
    }

    public static UserDailyVerificationCount create(String id) {
        return new UserDailyVerificationCount(id);
    }

    public void increment() {
        if (verificationCount >= MAX_VERIFICATION_COUNT) {
            throw new TooManyRequestsException("하루 인증 횟수를 초과했습니다.", "verification: " + id);
        }
        verificationCount++;
    }
}
