package in.koreatech.koin.domain.user.verification.model;

import java.util.Objects;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

import in.koreatech.koin._common.exception.CustomException;
import in.koreatech.koin._common.code.ApiResponseCode;
import in.koreatech.koin._common.util.random.VerificationNumberGenerator;
import lombok.Getter;
import lombok.ToString;

@Getter
@RedisHash(value = "userVerificationStatus")
@ToString
public class UserVerificationStatus {

    private static final long SMS_VERIFICATION_EXPIRATION_SECONDS = 60 * 3L; // 3분
    private static final long EMAIL_VERIFICATION_EXPIRATION_SECONDS = 60 * 5L; // 5분
    private static final long VERIFIED_EXPIRATION_SECONDS = 60 * 60L; // 1시간

    @Id
    private String id;

    private String verificationCode;

    private boolean isVerified = false;

    @TimeToLive
    private Long expiration;

    private UserVerificationStatus(String id, String verificationCode, Long expiration) {
        this.id = id;
        this.verificationCode = verificationCode;
        this.expiration = expiration;
    }

    public static UserVerificationStatus ofSms(String id, VerificationNumberGenerator generator) {
        return new UserVerificationStatus(id, generator.generate(), SMS_VERIFICATION_EXPIRATION_SECONDS);
    }

    public static UserVerificationStatus ofEmail(String id, VerificationNumberGenerator generator) {
        return new UserVerificationStatus(id, generator.generate(), EMAIL_VERIFICATION_EXPIRATION_SECONDS);
    }

    public void verify(String inputCode) {
        if (isCodeMismatched(inputCode)) {
            throw CustomException.of(ApiResponseCode.NOT_MATCHED_VERIFICATION_CODE, this);
        }
        this.isVerified = true;
        this.expiration = VERIFIED_EXPIRATION_SECONDS;
    }

    public void requireVerified() {
        if (isNotVerified()) {
            throw CustomException.of(ApiResponseCode.FORBIDDEN_VERIFICATION, this);
        }
    }

    private boolean isCodeMismatched(String inputCode) {
        return !Objects.equals(this.verificationCode, inputCode);
    }

    private boolean isNotVerified() {
        return !this.isVerified;
    }
}
