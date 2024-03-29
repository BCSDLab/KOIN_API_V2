package in.koreatech.koin.domain.owner.model;

import static lombok.AccessLevel.PRIVATE;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = PRIVATE)
@RedisHash(value = "owner@", timeToLive = 60 * 60 * 2)
public class OwnerInVerification {

    @Id
    private String email;

    private String certificationCode;

    public static OwnerInVerification of(String email, String certificationCode) {
        return new OwnerInVerification(email, certificationCode);
    }
}
