package in.koreatech.koin.domain.owner.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import lombok.Getter;

@Getter
@RedisHash(value = "owner@", timeToLive = 60 * 60 * 2)
public class OwnerInVerification {

    @Id
    private String email;
    private String certificationCode;
    private boolean isAuthed = false;

    public OwnerInVerification(String email, String certificationCode) {
        this.email = email;
        this.certificationCode = certificationCode;
    }

    public void verify() {
        this.isAuthed = true;
    }

    public static OwnerInVerification of(String email, String certificationCode) {
        return new OwnerInVerification(email, certificationCode);
    }
}
