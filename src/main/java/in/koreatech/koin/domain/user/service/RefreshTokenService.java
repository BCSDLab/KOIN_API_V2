package in.koreatech.koin.domain.user.service;

import java.util.Objects;
import java.util.UUID;

import org.springframework.stereotype.Service;

import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.model.UserToken;
import in.koreatech.koin.domain.user.repository.userTokenRedisRepository;
import in.koreatech.koin.global.auth.exception.AuthorizationException;
import in.koreatech.koin.global.exception.KoinIllegalArgumentException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private static final String REFRESH_TOKEN_FORMAT = "%s-%d";

    private final userTokenRedisRepository userTokenRedisRepository;

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
