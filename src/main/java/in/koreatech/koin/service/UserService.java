package in.koreatech.koin.service;

import in.koreatech.koin.domain.user.User;
import in.koreatech.koin.dto.UserLoginRequest;
import in.koreatech.koin.dto.UserLoginResponse;
import in.koreatech.koin.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public UserLoginResponse login(UserLoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new IllegalArgumentException("잘못된 로그인 정보입니다."));

        if (user.isSamePassword(request.getPassword())) {
            // TODO: jwt 토큰 발급
            return UserLoginResponse.of("token", "refreshToken", user.getUserType().getValue());
        } else {
            throw new IllegalArgumentException("잘못된 로그인 정보입니다.");
        }
    }
}
