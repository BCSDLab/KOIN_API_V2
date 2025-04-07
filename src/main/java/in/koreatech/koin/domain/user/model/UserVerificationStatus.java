package in.koreatech.koin.domain.user.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

import lombok.Builder;
import lombok.Getter;

@Getter
@RedisHash(value = "userVerificationStatus")
public class UserVerificationStatus {

    private static final long INITIAL_EXPIRATION_SECONDS = 60 * 3L; // 3분
    private static final long VERIFIED_EXPIRATION_SECONDS = 60 * 60L; // 1시간

    @Id
    private String id;

    private String verificationCode;

    private boolean verified = false;

    @TimeToLive
    private Long expiration;

    @Builder
    public UserVerificationStatus(String id, String verificationCode) {
        this.id = id;
        this.verificationCode = verificationCode;
        this.expiration = INITIAL_EXPIRATION_SECONDS;
    }

    public static UserVerificationStatus of(String id, String verificationCode) {
        return UserVerificationStatus.builder()
            .id(id)
            .verificationCode(verificationCode)
            .build();
    }

    public void markAsVerified() {
        this.verified = true;
        this.expiration = VERIFIED_EXPIRATION_SECONDS;
    }
}
