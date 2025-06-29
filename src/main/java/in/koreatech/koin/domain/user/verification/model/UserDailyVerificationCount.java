package in.koreatech.koin.domain.user.verification.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

import in.koreatech.koin._common.exception.CustomException;
import in.koreatech.koin._common.code.ApiResponseCode;
import in.koreatech.koin.domain.user.verification.config.VerificationProperties;
import lombok.Getter;
import lombok.ToString;

@Getter
@RedisHash(value = "userDailyVerificationCount")
@ToString
public class UserDailyVerificationCount {

    private static final long VERIFIED_EXPIRATION_SECONDS = 60 * 60 * 24L; // 24시간

    @Id
    private String id;

    private int maxVerificationCount;

    private int verificationCount;

    @TimeToLive
    private Long expiration;

    private UserDailyVerificationCount(String id, int maxVerificationCount) {
        this.id = id;
        this.verificationCount = 0;
        this.expiration = VERIFIED_EXPIRATION_SECONDS;
        this.maxVerificationCount = maxVerificationCount;
    }

    public static UserDailyVerificationCount of(String id, VerificationProperties verificationProperties) {
        return new UserDailyVerificationCount(id, verificationProperties.maxVerificationCount());
    }

    public void incrementVerificationCount() {
        if (verificationCount >= maxVerificationCount) {
            throw CustomException.of(ApiResponseCode.TOO_MANY_REQUESTS_VERIFICATION, this);
        }
        verificationCount++;
    }
}
