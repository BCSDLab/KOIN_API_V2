package in.koreatech.koin.service;

import in.koreatech.koin.auth.JwtProvider;
import in.koreatech.koin.domain.user.User;
import in.koreatech.koin.dto.UserLoginRequest;
import in.koreatech.koin.dto.UserLoginResponse;
import in.koreatech.koin.repository.UserRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;

    @Transactional
    public UserLoginResponse login(UserLoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new IllegalArgumentException("잘못된 로그인 정보입니다."));

        if (!user.isSamePassword(request.getPassword())) {
            throw new IllegalArgumentException("잘못된 로그인 정보입니다.");
        }

        String accessToken = jwtProvider.createToken(user);
        String refreshToken = String.format("%s%d", UUID.randomUUID(), user.getId());

        // TODO: access, refresh token Redis에 저장
        return UserLoginResponse.of(accessToken, "??", user.getUserType().getValue());
    }

}
