package in.koreatech.koin.domain.owner.model;

import java.util.concurrent.TimeUnit;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

import lombok.Getter;

@Getter
@RedisHash(value = "owner@")
public class OwnerInVerification {

    private static final long CACHE_EXPIRE_HOUR = 2L;

    @Id
    private String email;
    private String certificationCode;
    private boolean isAuthed = false;

    @TimeToLive(unit = TimeUnit.HOURS)
    private final Long expiration;

    public OwnerInVerification(String email, String certificationCode) {
        this.email = email;
        this.certificationCode = certificationCode;
        this.expiration = CACHE_EXPIRE_HOUR;
    }

    public void verify() {
        this.isAuthed = true;
    }

    public static OwnerInVerification of(String email, String certificationCode) {
        return new OwnerInVerification(email, certificationCode);
    }
}
