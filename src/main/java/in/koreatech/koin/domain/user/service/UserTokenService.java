package in.koreatech.koin.domain.user.service;

import java.util.Objects;
import java.util.UUID;

import org.springframework.stereotype.Service;

import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.model.UserToken;
import in.koreatech.koin.domain.user.repository.UserTokenRepository;
import in.koreatech.koin._common.auth.JwtProvider;
import in.koreatech.koin._common.auth.exception.AuthorizationException;
import in.koreatech.koin._common.exception.custom.KoinIllegalArgumentException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserTokenService {

    private final JwtProvider jwtProvider;
    private final UserTokenRepository userTokenRepository;

    public String createAccessToken(User user) {
        return jwtProvider.createToken(user);
    }

    public String generateRefreshToken(User user) {
        return String.format("%s-%d", UUID.randomUUID(), user.getId());
    }

    public void checkLoginStatus(String accessToken) {
        jwtProvider.getUserId(accessToken);
    }

    public UserToken validateRefreshToken(String refreshToken, Integer userId) {
        UserToken userToken = userTokenRepository.getById(userId);
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
