package in.koreatech.koin.domain.user.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

import lombok.Getter;

@Getter
@RedisHash(value = "userVerificationStatus")
public class UserVerificationStatus {

    private static final long CACHE_EXPIRE_SECOND = 60 * 3L;

    @Id
    private String id;
    private String certificationCode;

    @TimeToLive
    private Long expiration;

    public UserVerificationStatus(String id, String certificationCode) {
        this.id = id;
        this.certificationCode = certificationCode;
        this.expiration = CACHE_EXPIRE_SECOND;
    }

    public static UserVerificationStatus of(String id, String certificationCode) {
        return new UserVerificationStatus(id, certificationCode);
    }
}
