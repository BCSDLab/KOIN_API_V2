package in.koreatech.koin.domain.user.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

import lombok.Getter;

@Getter
@RedisHash(value = "smsAuthStatus")
public class SmsAuthedStatus {

    private static final long EXPIRATION_SECONDS = 60 * 60L; // 1시간

    @Id
    private String phoneNumber;

    @TimeToLive
    private Long expiration;

    public SmsAuthedStatus(String phoneNumber) {
        this.phoneNumber = phoneNumber;
        this.expiration = EXPIRATION_SECONDS;
    }

    public static SmsAuthedStatus from(String phoneNumber) {
        return new SmsAuthedStatus(phoneNumber);
    }
}
