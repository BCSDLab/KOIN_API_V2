package in.koreatech.koin.domain.user.service;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import in.koreatech.koin._common.auth.exception.AuthorizationException;
import in.koreatech.koin._common.exception.custom.KoinIllegalArgumentException;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.model.UserToken;
import in.koreatech.koin.domain.user.repository.UserTokenRedisRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private static final String REFRESH_TOKEN_FORMAT = "%s-%d";
    private static final long REFRESH_TOKEN_EXPIRE_DAY = 90L;

    private final UserTokenRedisRepository userTokenRedisRepository;
    private final RedisTemplate<String, String> redisTemplate;

    public void saveRefreshToken(Integer userId, String platform) {
        String key = getUserKey(userId, platform);
        redisTemplate.opsForValue().set(key, String.valueOf(UUID.randomUUID()), REFRESH_TOKEN_EXPIRE_DAY, TimeUnit.DAYS);
    }

    public String getRefreshToken(Integer userId, String platform) {
        String key = getUserKey(userId, platform);
        return redisTemplate.opsForValue().get(key);
    }

    public void deleteRefreshToken(Integer userId, String platform) {
        String key = getUserKey(userId, platform);
        redisTemplate.delete(key);
    }

    public void deleteAllRefreshTokens(Integer userId) {
        redisTemplate.delete(getUserKey(userId, "PC"));
        redisTemplate.delete(getUserKey(userId, "MOBILE"));
        redisTemplate.delete(getUserKey(userId, "TABLET"));
    }

    private String getUserKey(Integer userId, String platform) {
        return "refreshToken:" + userId + ":" + platform.toUpperCase();
    }

    public String createRefreshToken(User user) {
        return String.format(REFRESH_TOKEN_FORMAT, UUID.randomUUID(), user.getId());
    }

    public UserToken verifyAndGetUserToken(String refreshToken, Integer userId) {
        UserToken userToken = userTokenRedisRepository.getById(userId);
        if (!Objects.equals(userToken.getRefreshToken(), refreshToken)) {
            throw new KoinIllegalArgumentException("refresh token이 일치하지 않습니다.", "refreshToken: " + refreshToken);
        }
        return userToken;
    }

    public String extractUserId(String refreshToken) {
        String[] split = refreshToken.split("-");
        if (split.length == 0) {
            throw new AuthorizationException("올바르지 않은 인증 토큰입니다. refreshToken: " + refreshToken);
        }
        return split[split.length - 1];
    }
}
