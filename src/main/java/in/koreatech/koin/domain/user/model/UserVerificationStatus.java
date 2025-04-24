package in.koreatech.koin.domain.user.model;

import java.util.Objects;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

import lombok.Getter;

@Getter
@RedisHash(value = "userVerificationStatus")
public class UserVerificationStatus {

    private static final long SMS_VERIFICATION_EXPIRATION_SECONDS = 60 * 3L; // 3분
    private static final long EMAIL_VERIFICATION_EXPIRATION_SECONDS = 60 * 5L; // 5분
    private static final long VERIFIED_EXPIRATION_SECONDS = 60 * 60L; // 1시간

    @Id
    private String id;

    private String verificationCode;

    private boolean verified = false;

    @TimeToLive
    private Long expiration;

    private UserVerificationStatus(String id, String verificationCode, Long expiration) {
        this.id = id;
        this.verificationCode = verificationCode;
        this.expiration = expiration;
    }

    public static UserVerificationStatus ofSms(String id, String verificationCode) {
        return new UserVerificationStatus(id, verificationCode, SMS_VERIFICATION_EXPIRATION_SECONDS);
    }

    public static UserVerificationStatus ofEmail(String id, String verificationCode) {
        return new UserVerificationStatus(id, verificationCode, EMAIL_VERIFICATION_EXPIRATION_SECONDS);
    }

    public void markAsVerified() {
        this.verified = true;
        this.expiration = VERIFIED_EXPIRATION_SECONDS;
    }

    public boolean isCodeMismatched(String inputCode) {
        return !Objects.equals(this.verificationCode, inputCode);
    }
}
