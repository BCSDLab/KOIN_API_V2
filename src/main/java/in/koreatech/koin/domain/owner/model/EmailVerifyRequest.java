package in.koreatech.koin.domain.owner.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

import lombok.Getter;

/**
 * 이메일 재요청 시간제한
 */
@Getter
@RedisHash(value = "emailverify@")
public class EmailVerifyRequest {

    private static final long CACHE_EXPIRE_SECOND = 60L;

    @Id
    private String email;

    @TimeToLive
    private Long expiration;

    public EmailVerifyRequest(String email) {
        this.email = email;
        this.expiration = CACHE_EXPIRE_SECOND;
    }
}