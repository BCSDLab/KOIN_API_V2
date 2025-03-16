package in.koreatech.koin.domain.user.service;

import java.time.LocalDateTime;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin._common.auth.JwtProvider;
import in.koreatech.koin._common.event.UserDeleteEvent;
import in.koreatech.koin.admin.abtest.useragent.UserAgentInfo;
import in.koreatech.koin.domain.owner.repository.OwnerRepository;
import in.koreatech.koin.domain.student.repository.StudentRepository;
import in.koreatech.koin.domain.timetableV2.repository.TimetableFrameRepositoryV2;
import in.koreatech.koin.domain.user.dto.AuthResponse;
import in.koreatech.koin.domain.user.dto.UserLoginRequest;
import in.koreatech.koin.domain.user.dto.UserLoginResponse;
import in.koreatech.koin.domain.user.dto.UserTokenRefreshRequest;
import in.koreatech.koin.domain.user.dto.UserTokenRefreshResponse;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.model.UserType;
import in.koreatech.koin.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final OwnerRepository ownerRepository;
    private final TimetableFrameRepositoryV2 timetableFrameRepositoryV2;
    private final ApplicationEventPublisher eventPublisher;
    private final UserValidationService userValidationService;
    private final RefreshTokenService refreshTokenService;
    private final JwtProvider jwtProvider;

    @Transactional
    public UserLoginResponse login(UserLoginRequest request, UserAgentInfo userAgentInfo) {
        User user = userValidationService.checkLoginCredentials(request.email(), request.password());
        userValidationService.checkUserAuthentication(request.email());

        String accessToken = jwtProvider.createToken(user);
        String refreshToken = refreshTokenService.saveRefreshToken(user.getId(), userAgentInfo.getType());
        updateLastLoginTime(user);

        return UserLoginResponse.of(accessToken, refreshToken, user.getUserType().getValue());
    }

    @Transactional
    public void logout(Integer userId, UserAgentInfo userAgentInfo) {
        refreshTokenService.deleteRefreshToken(userId, userAgentInfo.getType());
    }

    public void checkLogin(String accessToken) {
        jwtProvider.getUserId(accessToken);
    }

    public AuthResponse getAuth(Integer userId) {
        User user = userRepository.getById(userId);
        return AuthResponse.from(user);
    }

    public UserTokenRefreshResponse refresh(UserTokenRefreshRequest request, UserAgentInfo userAgentInfo) {
        Integer userId = refreshTokenService.extractUserId(request.refreshToken());
        refreshTokenService.verifyRefreshToken(userId, userAgentInfo.getType(), request.refreshToken());

        User user = userRepository.getById(userId);
        String accessToken = jwtProvider.createToken(user);

        return UserTokenRefreshResponse.of(accessToken, refreshTokenService.getRefreshToken(userId, userAgentInfo.getType()));
    }

    @Transactional
    public void withdraw(Integer userId) {
        User user = userRepository.getById(userId);
        if (user.getUserType() == UserType.STUDENT) {
            timetableFrameRepositoryV2.deleteAllByUser(user);
            studentRepository.deleteByUserId(userId);
        } else if (user.getUserType() == UserType.OWNER) {
            ownerRepository.deleteByUserId(userId);
        }
        refreshTokenService.deleteAllRefreshTokens(userId);
        userRepository.delete(user);
        eventPublisher.publishEvent(new UserDeleteEvent(user.getEmail(), user.getUserType()));
    }

    public void updateLastLoginTime(User user) {
        user.updateLastLoggedTime(LocalDateTime.now());
    }
}
