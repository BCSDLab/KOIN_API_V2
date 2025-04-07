package in.koreatech.koin.domain.user.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

import lombok.Getter;

@Getter
@RedisHash(value = "smsVerificationStatus")
public class SmsVerificationStatus {

    private static final long EXPIRATION_SECONDS = 60 * 3L;

    @Id
    private String phoneNumber;

    private String verificationCode;

    @TimeToLive
    private Long expiration;

    public SmsVerificationStatus(String phoneNumber, String verificationCode) {
        this.phoneNumber = phoneNumber;
        this.verificationCode = verificationCode;
        this.expiration = EXPIRATION_SECONDS;
    }

    public static SmsVerificationStatus of(String phoneNumber, String verificationCode) {
        return new SmsVerificationStatus(phoneNumber, verificationCode);
    }
}
