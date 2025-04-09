package in.koreatech.koin.domain.user.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

import in.koreatech.koin._common.exception.custom.TooManyRequestsException;
import lombok.Builder;
import lombok.Getter;

@Getter
@RedisHash(value = "userDailyVerificationCount")
public class UserDailyVerificationCount {

    private static final long EXPIRATION_SECONDS = 60 * 60 * 24L;
    public static final int MAX_VERIFICATION_COUNT = 5;

    @Id
    private String id;

    private int verificationCount = 1;

    @TimeToLive
    private Long expiration;

    @Builder
    private UserDailyVerificationCount(String id) {
        this.id = id;
        this.expiration = EXPIRATION_SECONDS;
    }

    public static UserDailyVerificationCount from(String id) {
        return UserDailyVerificationCount.builder()
            .id(id)
            .build();
    }

    public void incrementVerificationCount() {
        if (verificationCount >= MAX_VERIFICATION_COUNT) {
            throw new TooManyRequestsException("하루 인증 횟수를 초과했습니다.", "verification: " + id);
        }
        verificationCount++;
    }
}
