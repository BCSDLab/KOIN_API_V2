package in.koreatech.koin.domain.owner.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

@RedisHash(value = "owner@", timeToLive = 60 * 60 * 2)
public record OwnerInVerification(
    @Id String email,
    String certificationCode
) {

    public static OwnerInVerification from(String email, String certificationCode) {
        return new OwnerInVerification(email, certificationCode);
    }
}
