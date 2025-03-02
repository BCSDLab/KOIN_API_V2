package in.koreatech.koin.domain.user.service;

import java.time.LocalDateTime;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.owner.repository.OwnerRepository;
import in.koreatech.koin.domain.student.repository.StudentRepository;
import in.koreatech.koin.domain.timetableV2.repository.TimetableFrameRepositoryV2;
import in.koreatech.koin.domain.user.dto.AuthResponse;
import in.koreatech.koin.domain.user.dto.UserLoginRequest;
import in.koreatech.koin.domain.user.dto.UserLoginResponse;
import in.koreatech.koin.domain.user.dto.UserTokenRefreshRequest;
import in.koreatech.koin.domain.user.dto.UserTokenRefreshResponse;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.model.UserDeleteEvent;
import in.koreatech.koin.domain.user.model.UserToken;
import in.koreatech.koin.domain.user.model.UserType;
import in.koreatech.koin.domain.user.repository.UserRepository;
import in.koreatech.koin.domain.user.repository.userTokenRedisRepository;
import in.koreatech.koin.global.auth.JwtProvider;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final OwnerRepository ownerRepository;
    private final userTokenRedisRepository userTokenRedisRepository;
    private final TimetableFrameRepositoryV2 timetableFrameRepositoryV2;
    private final ApplicationEventPublisher eventPublisher;
    private final UserValidationService userValidationService;
    private final RefreshTokenService refreshTokenService;
    private final JwtProvider jwtProvider;

    @Transactional
    public UserLoginResponse login(UserLoginRequest request) {
        User user = userValidationService.checkLoginCredentials(request.email(), request.password());
        userValidationService.checkUserAuthentication(request.email());

        String accessToken = jwtProvider.createToken(user);
        String refreshToken = refreshTokenService.createRefreshToken(user);
        UserToken savedToken = userTokenRedisRepository.save(UserToken.create(user.getId(), refreshToken));
        updateLastLoginTime(user);

        return UserLoginResponse.of(accessToken, savedToken.getRefreshToken(), user.getUserType().getValue());
    }

    @Transactional
    public void logout(Integer userId) {
        userTokenRedisRepository.deleteById(userId);
    }

    public void checkLogin(String accessToken) {
        jwtProvider.getUserId(accessToken);
    }

    public AuthResponse getAuth(Integer userId) {
        User user = userRepository.getById(userId);
        return AuthResponse.from(user);
    }

    public UserTokenRefreshResponse refresh(UserTokenRefreshRequest request) {
        String userId = refreshTokenService.extractUserId(request.refreshToken());
        UserToken userToken = refreshTokenService.verifyAndGetUserToken(request.refreshToken(), Integer.parseInt(userId));

        User user = userRepository.getById(userToken.getId());
        String accessToken = jwtProvider.createToken(user);

        return UserTokenRefreshResponse.of(accessToken, userToken.getRefreshToken());
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
        userRepository.delete(user);
        eventPublisher.publishEvent(new UserDeleteEvent(user.getEmail(), user.getUserType()));
    }

    public void updateLastLoginTime(User user) {
        user.updateLastLoggedTime(LocalDateTime.now());
    }
}
