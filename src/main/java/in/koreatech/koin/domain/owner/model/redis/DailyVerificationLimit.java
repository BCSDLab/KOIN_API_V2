package in.koreatech.koin.domain.owner.model.redis;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

import in.koreatech.koin.domain.owner.exception.VerificationDailyLimitExceededException;
import lombok.Getter;

/**
 * 인증 요청 하루 제한 횟수
 */
@Getter
@RedisHash(value = "dailyVerifyCount@")
public class DailyVerificationLimit {

    private static final long CACHE_EXPIRE_SECOND = 60 * 60 * 24L;

    @Id
    private String key;

    private Integer requestCount = 1;

    @TimeToLive
    private Long expiration;

    public DailyVerificationLimit(String key) {
        this.key = key;
        this.expiration = CACHE_EXPIRE_SECOND;
    }

    public void requestVerification() {
        if (requestCount >= 5) {
            throw VerificationDailyLimitExceededException.withDetail("limit: 5");
        }
        ++requestCount;
    }
}
