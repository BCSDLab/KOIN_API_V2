package in.koreatech.koin.domain.user.service;

import in.koreatech.koin.domain.user.exception.UserNotFoundException;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.auth.JwtProvider;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.model.UserToken;
import in.koreatech.koin.domain.user.dto.UserLoginRequest;
import in.koreatech.koin.domain.user.dto.UserLoginResponse;
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
        User user = userRepository.findByEmail(request.email())
            .orElseThrow(() -> UserNotFoundException.witDetail("request: " + request));

        if (!user.isSamePassword(request.password())) {
            throw new IllegalArgumentException("잘못된 로그인 정보입니다. request: " + request);
        }

        String accessToken = jwtProvider.createToken(user);
        String refreshToken = String.format("%s%d", UUID.randomUUID(), user.getId());
        UserToken savedToken = userTokenRepository.save(UserToken.create(user.getId(), refreshToken));
        user.updateLastLoggedTime(LocalDateTime.now());
        User saved = userRepository.save(user);

        return UserLoginResponse.of(accessToken, savedToken.getRefreshToken(), saved.getUserType().getValue());
    }
}
