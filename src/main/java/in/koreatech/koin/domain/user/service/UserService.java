package in.koreatech.koin.domain.user.service;

import static in.koreatech.koin.domain.user.model.UserType.*;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin._common.auth.JwtProvider;
import in.koreatech.koin._common.auth.exception.AuthenticationException;
import in.koreatech.koin._common.event.UserDeleteEvent;
import in.koreatech.koin._common.exception.custom.KoinIllegalArgumentException;
import in.koreatech.koin.domain.owner.repository.OwnerRepository;
import in.koreatech.koin.domain.student.repository.StudentRepository;
import in.koreatech.koin.domain.timetableV2.repository.TimetableFrameRepositoryV2;
import in.koreatech.koin.domain.user.dto.AuthResponse;
import in.koreatech.koin.domain.user.dto.RefreshUserTokenRequest;
import in.koreatech.koin.domain.user.dto.RefreshUserTokenResponse;
import in.koreatech.koin.domain.user.dto.RegisterUserRequest;
import in.koreatech.koin.domain.user.dto.UpdateUserRequest;
import in.koreatech.koin.domain.user.dto.UpdateUserResponse;
import in.koreatech.koin.domain.user.dto.UserLoginRequest;
import in.koreatech.koin.domain.user.dto.UserLoginRequestV2;
import in.koreatech.koin.domain.user.dto.UserLoginResponse;
import in.koreatech.koin.domain.user.dto.UserResponse;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.model.UserToken;
import in.koreatech.koin.domain.user.model.UserType;
import in.koreatech.koin.domain.user.repository.UserRepository;
import in.koreatech.koin.domain.user.repository.UserTokenRedisRepository;
import in.koreatech.koin.domain.user.verification.service.UserVerificationService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final OwnerRepository ownerRepository;
    private final UserTokenRedisRepository userTokenRedisRepository;
    private final UserVerificationService userVerificationService;
    private final TimetableFrameRepositoryV2 timetableFrameRepositoryV2;
    private final ApplicationEventPublisher eventPublisher;
    private final UserValidationService userValidationService;
    private final RefreshTokenService refreshTokenService;
    private final JwtProvider jwtProvider;
    private final PasswordEncoder passwordEncoder;

    public UserResponse getUserV2(Integer userId) {
        User user = userRepository.getById(userId);
        return UserResponse.from(user);
    }

    @Transactional
    public UpdateUserResponse updateUserV2(Integer userId, UpdateUserRequest request) {
        User user = userRepository.getById(userId);
        userValidationService.checkDuplicatedUpdateNickname(request.nickname(), userId);
        userValidationService.checkDuplicatedUpdateEmail(request.email(), userId);
        userValidationService.checkDuplicatedUpdatePhoneNumber(request.phoneNumber(), userId);
        user.update(request.email(), request.nickname(), request.name(), request.phoneNumber(), request.gender());
        return UpdateUserResponse.from(user);
    }

    @Transactional
    public void userRegister(RegisterUserRequest request) {
        userValidationService.checkDuplicationUserData(
            request.nickname(),
            request.email(),
            request.phoneNumber(),
            request.loginId()
        );
        User user = request.toUser(passwordEncoder);
        userRepository.save(user);
        // eventPublisher.publishEvent(new UserRegisterEvent(user.getId(), request.marketingNotificationAgreement()));
        userVerificationService.consumeVerification(request.phoneNumber());
    }

    @Transactional
    public UserLoginResponse loginV2(UserLoginRequestV2 request) {
        User user = userValidationService.checkLoginCredentialsV2(request.loginId(), request.loginPw());
        return createLoginResponse(user);
    }

    @Transactional
    public UserLoginResponse login(UserLoginRequest request) {
        User user = userValidationService.checkLoginCredentials(request.email(), request.password());
        userValidationService.checkUserAuthentication(request.email());
        return createLoginResponse(user);
    }

    private UserLoginResponse createLoginResponse(User user) {
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

    public RefreshUserTokenResponse refresh(RefreshUserTokenRequest request) {
        String userId = refreshTokenService.extractUserId(request.refreshToken());
        UserToken userToken = refreshTokenService.verifyAndGetUserToken(request.refreshToken(), Integer.parseInt(userId));

        User user = userRepository.findById(userToken.getId())
            .orElseThrow(() -> AuthenticationException.withDetail("유효하지 않은 토큰입니다. userId : " + userId));

        String accessToken = jwtProvider.createToken(user);
        return RefreshUserTokenResponse.of(accessToken, userToken.getRefreshToken());
    }

    @Transactional
    public void withdraw(Integer userId) {
        User user = userRepository.getById(userId);
        if (user.getUserType() == STUDENT) {
            timetableFrameRepositoryV2.deleteAllByUser(user);
            studentRepository.deleteByUserId(userId);
        } else if (user.getUserType() == OWNER) {
            ownerRepository.deleteByUserId(userId);
        }
        userRepository.delete(user);
        eventPublisher.publishEvent(new UserDeleteEvent(user.getEmail(), user.getUserType()));
    }

    public void updateLastLoginTime(User user) {
        user.updateLastLoggedTime(LocalDateTime.now());
    }

    public String findIdBySms(String phoneNumber) {
        User user = userRepository.getByPhoneNumberAndUserTypeIn(phoneNumber, List.of(GENERAL, STUDENT));
        String userId = user.getUserId();
        userVerificationService.consumeVerification(phoneNumber);
        return userId;
    }

    public String findIdByEmail(String email) {
        User user = userRepository.getByEmailAndUserTypeIn(email, List.of(GENERAL, STUDENT));
        String userId = user.getUserId();
        userVerificationService.consumeVerification(email);
        return userId;
    }

    @Transactional
    public void resetPasswordBySms(String userId, String phoneNumber, String newPassword) {
        User user = userRepository.getByUserIdAndUserTypeIn(userId, List.of(GENERAL, STUDENT));
        if (user.isNotSamePhoneNumber(phoneNumber)) {
            throw new KoinIllegalArgumentException("입력한 아이디와 인증된 사용자 정보가 일치하지 않습니다.");
        }
        user.updatePassword(passwordEncoder, newPassword);
        userRepository.save(user);
        userVerificationService.consumeVerification(phoneNumber);
    }

    @Transactional
    public void resetPasswordByEmail(String userId, String email, String newPassword) {
        User user = userRepository.getByUserIdAndUserTypeIn(userId, List.of(GENERAL, STUDENT));
        if (user.isNotSameEmail(email)) {
            throw new KoinIllegalArgumentException("입력한 아이디와 인증된 사용자 정보가 일치하지 않습니다.");
        }
        user.updatePassword(passwordEncoder, newPassword);
        userRepository.save(user);
        userVerificationService.consumeVerification(email);
    }
}
