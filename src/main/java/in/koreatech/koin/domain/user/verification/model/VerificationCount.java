package in.koreatech.koin.domain.user.verification.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;
import org.springframework.util.StringUtils;

import in.koreatech.koin._common.exception.CustomException;
import in.koreatech.koin._common.code.ApiResponseCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@RedisHash("userDailyVerificationCount")
@ToString
public class VerificationCount {

    private static final long VERIFIED_EXPIRATION_SECONDS = 60 * 60 * 24L; // 24시간
    private static final String KEY_FORMAT = "%s:%s";

    @Id
    private String id;

    private int maxVerificationCount;

    private int verificationCount;

    @TimeToLive
    private Long expiration;

    private VerificationCount(String id, int maxVerificationCount) {
        this.id = id;
        this.maxVerificationCount = maxVerificationCount;
        this.verificationCount = 0;
        this.expiration = VERIFIED_EXPIRATION_SECONDS;
    }

    public static String composeKey(String id, String ip) {
        return StringUtils.hasText(ip) ? String.format(KEY_FORMAT, id, ip) : id;
    }

    public static VerificationCount of(String id, String ip, int maxVerificationCount) {
        return new VerificationCount(composeKey(id, ip), maxVerificationCount);
    }

    public void incrementVerificationCount() {
        if (verificationCount >= maxVerificationCount) {
            throw CustomException.of(ApiResponseCode.TOO_MANY_REQUESTS_VERIFICATION, "count: " + verificationCount);
        }
        verificationCount++;
    }
}
