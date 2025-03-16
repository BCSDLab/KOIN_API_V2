package in.koreatech.koin.domain.user.service;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin._common.auth.exception.AuthorizationException;
import in.koreatech.koin._common.auth.exception.RefreshTokenNotFoundException;
import in.koreatech.koin._common.exception.custom.KoinIllegalArgumentException;
import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class RefreshTokenService {

    private static final long REFRESH_TOKEN_EXPIRE_DAY = 90L;

    private final RedisTemplate<String, String> redisTemplate;

    @Transactional
    public String createRefreshToken(Integer userId, String platform) {
        String key = getUserKey(userId, platform);
        redisTemplate.opsForValue().set(key, UUID.randomUUID() + "-" + userId, REFRESH_TOKEN_EXPIRE_DAY, TimeUnit.DAYS);
        return redisTemplate.opsForValue().get(key);
    }

    public String getRefreshToken(Integer userId, String platform) {
        String key = getUserKey(userId, platform);
        return redisTemplate.opsForValue().get(key);
    }

    public void verifyRefreshToken(Integer userId, String platform, String refreshToken) {
        String key = getUserKey(userId, platform);
        String savedRefreshToken = redisTemplate.opsForValue().get(key);
        if (savedRefreshToken == null) {
            throw RefreshTokenNotFoundException.withDetail("userId : " + userId + " platform : " + platform);
        }
        if (!Objects.equals(savedRefreshToken, refreshToken)) {
            throw new KoinIllegalArgumentException("refresh token이 일치하지 않습니다.", "refreshToken: " + refreshToken);
        }
    }

    @Transactional
    public void deleteRefreshToken(Integer userId, String platform) {
        String key = getUserKey(userId, platform);
        redisTemplate.delete(key);
    }

    @Transactional
    public void deleteAllRefreshTokens(Integer userId) {
        redisTemplate.delete(getUserKey(userId, "PC"));
        redisTemplate.delete(getUserKey(userId, "MOBILE"));
        redisTemplate.delete(getUserKey(userId, "TABLET"));
    }

    private String getUserKey(Integer userId, String platform) {
        return "refreshToken:" + userId + ":" + platform.toUpperCase();
    }

    public Integer extractUserId(String refreshToken) {
        String[] split = refreshToken.split("-");
        if (split.length == 0) {
            throw new AuthorizationException("올바르지 않은 인증 토큰입니다. refreshToken: " + refreshToken);
        }
        return Integer.parseInt(split[split.length - 1]);
    }
}
