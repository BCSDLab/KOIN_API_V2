package in.koreatech.koin.domain.user.verification.model;

import java.util.Objects;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

import in.koreatech.koin._common.auth.exception.AuthorizationException;
import in.koreatech.koin._common.exception.custom.KoinIllegalArgumentException;
import in.koreatech.koin._common.util.random.VerificationNumberGenerator;
import lombok.Getter;

@Getter
@RedisHash(value = "userVerificationStatus")
public class UserVerificationStatus {

    private static final long SMS_VERIFICATION_EXPIRATION_SECONDS = 60 * 3L; // 3분
    private static final long EMAIL_VERIFICATION_EXPIRATION_SECONDS = 60 * 5L; // 5분
    private static final long VERIFIED_EXPIRATION_SECONDS = 60 * 60L; // 1시간

    @Id
    private String id;

    private final String verificationCode;

    private boolean verified = false;

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
            throw new KoinIllegalArgumentException("인증 번호가 일치하지 않습니다.");
        }
        this.verified = true;
        this.expiration = VERIFIED_EXPIRATION_SECONDS;
    }

    public void requireVerified() {
        if (isNotVerified()) {
            throw new AuthorizationException("본인 인증 후 다시 시도해주십시오.");
        }
    }

    private boolean isCodeMismatched(String inputCode) {
        return !Objects.equals(this.verificationCode, inputCode);
    }

    private boolean isNotVerified() {
        return !this.verified;
    }
}
