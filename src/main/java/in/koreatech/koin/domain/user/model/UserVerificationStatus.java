package in.koreatech.koin.domain.user.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

import lombok.Getter;

@Getter
@RedisHash(value = "userVerificationStatus@")
public class UserVerificationStatus {

    private static final long CACHE_EXPIRE_SECOND = 60 * 3L;

    @Id
    private String key;
    private String certificationCode;

    @TimeToLive
    private Long expiration;

    public UserVerificationStatus(String key, String certificationCode) {
        this.key = key;
        this.certificationCode = certificationCode;
        this.expiration = CACHE_EXPIRE_SECOND;
    }

    public static UserVerificationStatus of(String key, String certificationCode) {
        return new UserVerificationStatus(key, certificationCode);
    }
}
