package in.koreatech.koin.domain.user.service;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.auth.JwtProvider;
import in.koreatech.koin.domain.auth.exception.AuthException;
import in.koreatech.koin.domain.user.dto.UserLoginRequest;
import in.koreatech.koin.domain.user.dto.UserLoginResponse;
import in.koreatech.koin.domain.user.dto.UserTokenRefreshRequest;
import in.koreatech.koin.domain.user.dto.UserTokenRefreshResponse;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.model.UserToken;
import in.koreatech.koin.domain.user.repository.UserRepository;
import in.koreatech.koin.domain.user.repository.UserTokenRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final JwtProvider jwtProvider;
    private final UserRepository userRepository;
    private final UserTokenRepository userTokenRepository;

    @Transactional
    public UserLoginResponse login(UserLoginRequest request) {
        User user = userRepository.getByEmail(request.email());

        if (!user.isSamePassword(request.password())) {
            throw new IllegalArgumentException("비밀번호가 틀렸습니다. request: " + request);
        }

        String accessToken = jwtProvider.createToken(user);
        String refreshToken = String.format("%s-%d", UUID.randomUUID(), user.getId());
        UserToken savedToken = userTokenRepository.save(UserToken.create(user.getId(), refreshToken));
        user.updateLastLoggedTime(LocalDateTime.now());
        User saved = userRepository.save(user);

        return UserLoginResponse.of(accessToken, savedToken.getRefreshToken(), saved.getUserType().getValue());
    }

    @Transactional
    public void logout(Long userId) {
        userTokenRepository.deleteById(userId);
    }

    public UserTokenRefreshResponse refresh(UserTokenRefreshRequest request) {
        String userId = getUserId(request.refreshToken());
        UserToken userToken = userTokenRepository.getById(Long.parseLong(userId));
        if (!Objects.equals(userToken.getRefreshToken(), request.refreshToken())) {
            throw new IllegalArgumentException("refresh token이 일치하지 않습니다. request: " + request);
        }
        User user = userRepository.getById(userToken.getId());

        String accessToken = jwtProvider.createToken(user);
        return UserTokenRefreshResponse.of(accessToken, userToken.getRefreshToken());
    }

    private static String getUserId(String refreshToken) {
        String[] split = refreshToken.split("-");
        if (split.length == 0) {
            throw new AuthException("올바르지 않은 인증 토큰입니다. refreshToken: " + refreshToken);
        }
        return split[split.length - 1];
    }
}
