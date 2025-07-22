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
public class VerificationCode {

    private static final long SMS_VERIFICATION_EXPIRATION_SECONDS = 60 * 3L; // 3분
    private static final long EMAIL_VERIFICATION_EXPIRATION_SECONDS = 60 * 5L; // 5분
    private static final long VERIFIED_EXPIRATION_SECONDS = 60 * 30L; // 30분
    private static final int MAX_TRIAL_COUNT = 10;

    @Id
    private String id;

    private String verificationCode;

    private int trialCount;

    private boolean isVerified;

    @TimeToLive
    private Long expiration;

    private VerificationCode(String id, String verificationCode, Long expiration) {
        this.id = id;
        this.verificationCode = verificationCode;
        this.trialCount = 0;
        this.isVerified = false;
        this.expiration = expiration;
    }

    public static VerificationCode of(String id, VerificationNumberGenerator generator, VerificationChannel channel) {
        return switch (channel) {
            case SMS -> new VerificationCode(id, generator.generate(), SMS_VERIFICATION_EXPIRATION_SECONDS);
            case EMAIL -> new VerificationCode(id, generator.generate(), EMAIL_VERIFICATION_EXPIRATION_SECONDS);
        };
    }

    public void detectAbnormalUsage() {
        if (trialCount >= MAX_TRIAL_COUNT) {
            throw CustomException.of(ApiResponseCode.NOT_MATCHED_VERIFICATION_CODE, "trialCount: " + trialCount);
        }
    }

    public void verify(String inputCode) {
        if (isCodeMismatched(inputCode)) {
            trialCount++;
            throw CustomException.of(ApiResponseCode.NOT_MATCHED_VERIFICATION_CODE, "inputCode: " + inputCode);
        }
        this.isVerified = true;
        this.expiration = VERIFIED_EXPIRATION_SECONDS;
    }

    public void requireVerified() {
        if (isNotVerified()) {
            throw CustomException.of(ApiResponseCode.FORBIDDEN_VERIFICATION, "VerificationCodeId: " + id);
        }
    }

    private boolean isCodeMismatched(String inputCode) {
        return !Objects.equals(this.verificationCode, inputCode);
    }

    private boolean isNotVerified() {
        return !this.isVerified;
    }
}
