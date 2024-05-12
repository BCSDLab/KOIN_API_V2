package in.koreatech.koin.domain.owner.model.redis;

import java.util.concurrent.TimeUnit;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

import lombok.Getter;

@Getter
@RedisHash(value = "verificationStatus@")
public class OwnerVerificationStatus {

    private static final long CACHE_EXPIRE_HOUR = 2L;

    @Id
    private String key;
    private String certificationCode;

    @TimeToLive(unit = TimeUnit.HOURS)
    private Long expiration;

    public OwnerVerificationStatus(String key, String certificationCode) {
        this.key = key;
        this.certificationCode = certificationCode;
        this.expiration = CACHE_EXPIRE_HOUR;
    }

    public static OwnerVerificationStatus of(String key, String certificationCode) {
        return new OwnerVerificationStatus(key, certificationCode);
    }
}
