package in.koreatech.koin.domain.user.service;

import java.time.LocalDateTime;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.owner.repository.OwnerRepository;
import in.koreatech.koin.domain.timetableV2.repository.TimetableFrameRepositoryV2;
import in.koreatech.koin.domain.user.dto.AuthResponse;
import in.koreatech.koin.domain.user.dto.CoopResponse;
import in.koreatech.koin.domain.user.dto.UserLoginRequest;
import in.koreatech.koin.domain.user.dto.UserLoginResponse;
import in.koreatech.koin.domain.user.dto.UserTokenRefreshRequest;
import in.koreatech.koin.domain.user.dto.UserTokenRefreshResponse;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.model.UserDeleteEvent;
import in.koreatech.koin.domain.user.model.UserToken;
import in.koreatech.koin.domain.user.model.UserType;
import in.koreatech.koin.domain.student.repository.StudentRepository;
import in.koreatech.koin.domain.user.repository.UserRepository;
import in.koreatech.koin.domain.user.repository.UserTokenRedisRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final OwnerRepository ownerRepository;
    private final UserTokenRedisRepository userTokenRedisRepository;
    private final TimetableFrameRepositoryV2 timetableFrameRepositoryV2;
    private final ApplicationEventPublisher eventPublisher;
    private final UserValidationService userValidationService;
    private final UserTokenService userTokenService;

    @Transactional
    public UserLoginResponse login(UserLoginRequest request) {
        // TODO : Validation과 check　분리하는　기준　확실하게　하기
        User user = userValidationService.checkLoginCredentials(request.email(), request.password());
        // TODO: validation이 나을 듯
        userValidationService.checkUserAuthentication(request.email());

        // TODO : create와 generate 통일하기
        String accessToken = userTokenService.createAccessToken(user);
        String refreshToken = userTokenService.generateRefreshToken(user);
        UserToken savedToken = userTokenRedisRepository.save(UserToken.create(user.getId(), refreshToken));
        updateLastLoginTime(user);

        return UserLoginResponse.of(accessToken, savedToken.getRefreshToken(), user.getUserType().getValue());
    }

    @Transactional
    public void logout(Integer userId) {
        userTokenRedisRepository.deleteById(userId);
    }

    public void checkLogin(String accessToken) {
        userTokenService.checkLoginStatus(accessToken);
    }

    public AuthResponse getAuth(Integer userId) {
        User user = userRepository.getById(userId);
        return AuthResponse.from(user);
    }

    public UserTokenRefreshResponse refresh(UserTokenRefreshRequest request) {
        String userId = userTokenService.extractUserId(request.refreshToken());
        UserToken userToken = userTokenService.validateRefreshToken(request.refreshToken(), Integer.parseInt(userId));

        User user = userRepository.getById(userToken.getId());
        String accessToken = userTokenService.createAccessToken(user);

        return UserTokenRefreshResponse.of(accessToken, userToken.getRefreshToken());
    }

    @Transactional
    public void withdraw(Integer userId) {
        User user = userRepository.getById(userId);
        if (user.getUserType() == UserType.STUDENT) {
            // TODO : ON CASCADE 걸어서 삭제하는 방향
            timetableFrameRepositoryV2.deleteAllByUser(user);
            studentRepository.deleteByUserId(userId);
        } else if (user.getUserType() == UserType.OWNER) {
            ownerRepository.deleteByUserId(userId);
        }
        userRepository.delete(user);
        eventPublisher.publishEvent(new UserDeleteEvent(user.getEmail(), user.getUserType()));
    }

    // TODO: 이거 옮길 예정
    public CoopResponse getCoop(Integer userId) {
        User user = userRepository.getById(userId);
        return CoopResponse.from(user);
    }

    // TODO : User로 뺴기
    public void updateLastLoginTime(User user) {
        user.updateLastLoggedTime(LocalDateTime.now());
    }
}
