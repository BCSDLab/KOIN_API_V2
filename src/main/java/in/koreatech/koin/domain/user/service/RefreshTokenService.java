package in.koreatech.koin.domain.user.service;

import java.util.Objects;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin._common.auth.exception.AuthorizationException;
import in.koreatech.koin._common.exception.custom.KoinIllegalArgumentException;
import in.koreatech.koin.domain.user.model.RefreshToken;
import in.koreatech.koin.domain.user.repository.RefreshTokenRedisRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RefreshTokenService {

    private final RefreshTokenRedisRepository refreshTokenRedisRepository;

    public String createRefreshToken(Integer userId, String platform) {
        RefreshToken refreshToken = RefreshToken.create(userId, String.valueOf(UUID.randomUUID()), platform);
        return refreshTokenRedisRepository.save(refreshToken).getToken();
    }

    public String getRefreshToken(Integer userId, String platform) {
        String key = RefreshToken.generateKey(userId, platform);
        return refreshTokenRedisRepository.getById(key).getToken();
    }

    public void verifyRefreshToken(Integer userId, String platform, String refreshToken) {
        String key = RefreshToken.generateKey(userId, platform);
        String savedRefreshToken = refreshTokenRedisRepository.getById(key).getToken();
        if (!Objects.equals(savedRefreshToken, refreshToken)) {
            throw new KoinIllegalArgumentException("refresh token이 일치하지 않습니다.", "refreshToken: " + refreshToken);
        }
    }

    @Transactional
    public void deleteRefreshToken(Integer userId, String platform) {
        String key = RefreshToken.generateKey(userId, platform);
        refreshTokenRedisRepository.deleteById(key);
    }

    @Transactional
    public void deleteAllRefreshTokens(Integer userId) {
        refreshTokenRedisRepository.deleteById(RefreshToken.generateKey(userId, "PC"));
        refreshTokenRedisRepository.deleteById(RefreshToken.generateKey(userId, "MOBILE"));
        refreshTokenRedisRepository.deleteById(RefreshToken.generateKey(userId, "TABLET"));
    }

    public Integer extractUserId(String refreshToken) {
        String[] split = refreshToken.split("-");
        if (split.length == 0) {
            throw new AuthorizationException("올바르지 않은 인증 토큰입니다. refreshToken: " + refreshToken);
        }
        return Integer.parseInt(split[split.length - 1]);
    }
}
