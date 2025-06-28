package in.koreatech.koin.domain.user.verification.model;

import java.util.Objects;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

import in.koreatech.koin._common.exception.CustomException;
import in.koreatech.koin._common.exception.errorcode.ErrorCode;
import in.koreatech.koin._common.util.random.VerificationNumberGenerator;
import lombok.Getter;
import lombok.ToString;

@Getter
@RedisHash(value = "userVerificationStatus")
@ToString
public class VerificationCode {

    private static final long SMS_VERIFICATION_EXPIRATION_SECONDS = 60 * 3L; // 3분
    private static final long EMAIL_VERIFICATION_EXPIRATION_SECONDS = 60 * 5L; // 5분
    private static final long VERIFIED_EXPIRATION_SECONDS = 60 * 60L; // 1시간
    private static final int MAX_TRIAL_COUNT = 100;

    @Id
    private String id;

    private String verificationCode;

    private int trialCount;

    private boolean isVerified = false;

    @TimeToLive
    private Long expiration;

    private VerificationCode(String id, String verificationCode, Long expiration) {
        this.id = id;
        this.verificationCode = verificationCode;
        this.expiration = expiration;
        this.trialCount = 0;
    }

    public static VerificationCode ofSms(String id, VerificationNumberGenerator generator) {
        return new VerificationCode(id, generator.generate(), SMS_VERIFICATION_EXPIRATION_SECONDS);
    }

    public static VerificationCode ofEmail(String id, VerificationNumberGenerator generator) {
        return new VerificationCode(id, generator.generate(), EMAIL_VERIFICATION_EXPIRATION_SECONDS);
    }

    public static VerificationCode of(String id, VerificationNumberGenerator generator, VerificationChannel channel) {
        return switch (channel) {
            case SMS -> new VerificationCode(id, generator.generate(), SMS_VERIFICATION_EXPIRATION_SECONDS);
            case EMAIL -> new VerificationCode(id, generator.generate(), EMAIL_VERIFICATION_EXPIRATION_SECONDS);
        };
    }

    public void detectAbnormalUsage() {
        if (trialCount >= MAX_TRIAL_COUNT) {
            throw CustomException.of(ErrorCode.NOT_MATCHED_VERIFICATION_CODE, this);
        }
    }

    public void verify(String inputCode) {
        if (isCodeMismatched(inputCode)) {
            trialCount++;
            throw CustomException.of(ErrorCode.NOT_MATCHED_VERIFICATION_CODE, this);
        }
        this.isVerified = true;
        this.expiration = VERIFIED_EXPIRATION_SECONDS;
    }

    public void requireVerified() {
        if (isNotVerified()) {
            throw CustomException.of(ErrorCode.FORBIDDEN_API, this);
        }
    }

    private boolean isCodeMismatched(String inputCode) {
        return !Objects.equals(this.verificationCode, inputCode);
    }

    private boolean isNotVerified() {
        return !this.isVerified;
    }
}
