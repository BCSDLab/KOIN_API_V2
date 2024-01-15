package in.koreatech.koin.domain.user.service;

import in.koreatech.koin.domain.auth.JwtProvider;
import in.koreatech.koin.domain.auth.exception.AuthException;
import in.koreatech.koin.domain.user.dto.UserLoginRequest;
import in.koreatech.koin.domain.user.dto.UserLoginResponse;
import in.koreatech.koin.domain.user.dto.UserTokenRefreshRequest;
import in.koreatech.koin.domain.user.dto.UserTokenRefreshResponse;
import in.koreatech.koin.domain.user.exception.UserNotFoundException;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.model.UserToken;
import in.koreatech.koin.domain.user.repository.UserRepository;
import in.koreatech.koin.domain.user.repository.UserTokenRepository;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final JwtProvider jwtProvider;
    private final UserRepository userRepository;
    private final UserTokenRepository userTokenRepository;

    @Transactional
    public UserLoginResponse login(UserLoginRequest request) {
        User user = userRepository.findByEmail(request.email())
            .orElseThrow(() -> UserNotFoundException.withDetail("request: " + request));

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
    public void logout(User user) {
        userTokenRepository.deleteById(user.getId());
    }

    public UserTokenRefreshResponse refresh(UserTokenRefreshRequest request) {
        String userId = getUserId(request.refreshToken());
        UserToken userToken = userTokenRepository.findById(Long.parseLong(userId))
            .orElseThrow(() -> new IllegalArgumentException("refresh token이 존재하지 않습니다. request: " + request));
        if (!Objects.equals(userToken.getRefreshToken(), request.refreshToken())) {
            throw new IllegalArgumentException("refresh token이 일치하지 않습니다. request: " + request);
        }
        User user = userRepository.findById(userToken.getId())
            .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다. refreshToken: " + userToken));
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
