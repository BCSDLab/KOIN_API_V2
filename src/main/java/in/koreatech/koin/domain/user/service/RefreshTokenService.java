package in.koreatech.koin.domain.user.service;

import java.util.Objects;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.global.exception.CustomException;
import in.koreatech.koin.global.code.ApiResponseCode;
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
            throw CustomException.of(ApiResponseCode.NOT_MATCHED_REFRESH_TOKEN, "refreshToken: " + refreshToken);
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
            throw CustomException.of(ApiResponseCode.INVALID_REFRESH_TOKEN, "refreshToken: " + refreshToken);
        }
        return Integer.parseInt(split[split.length - 1]);
    }
}
