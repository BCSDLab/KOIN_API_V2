package in.koreatech.koin.domain.user.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

import lombok.Builder;
import lombok.Getter;

@Getter
@RedisHash(value = "userVerificationStatus")
public class UserVerificationStatus {

    private static final long VERIFIED_EXPIRATION_SECONDS = 60 * 60L; // 1시간

    @Id
    private String id;

    private String verificationCode;

    private boolean verified = false;

    @Getter
    @TimeToLive
    private Long expiration;

    @Builder
    private UserVerificationStatus(String id, String verificationCode, Long expiration) {
        this.id = id;
        this.verificationCode = verificationCode;
        this.expiration = expiration;
    }

    public static UserVerificationStatus of(String id, String verificationCode, Long expiration) {
        return UserVerificationStatus.builder()
            .id(id)
            .verificationCode(verificationCode)
            .expiration(expiration)
            .build();
    }

    public boolean isNotVerified() {
        return !verified;
    }

    public void markAsVerified() {
        this.verified = true;
        this.expiration = VERIFIED_EXPIRATION_SECONDS;
    }
}
