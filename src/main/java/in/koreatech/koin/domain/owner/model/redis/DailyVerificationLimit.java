package in.koreatech.koin.domain.owner.model.redis;

import java.util.concurrent.TimeUnit;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

import in.koreatech.koin.domain.owner.exception.RequestVerificationLimitException;
import lombok.Getter;

/**
 * 인증 요청 하루 제한 횟수
 */
@Getter
@RedisHash(value = "dailyVerifyCount@")
public class DailyVerificationLimit {

    private static final long CACHE_EXPIRE_DAYS = 1L;

    @Id
    private String key;

    private Integer requestCount = 1;

    @TimeToLive(unit = TimeUnit.DAYS)
    private Long expiration;

    public DailyVerificationLimit(String key) {
        this.key = key;
        this.expiration = CACHE_EXPIRE_DAYS;
    }

    public void requestVerification() {
        if (requestCount >= 5) throw RequestVerificationLimitException.withDetail("limit: 5");
        ++requestCount;
    }
}
