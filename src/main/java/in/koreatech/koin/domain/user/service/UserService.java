package in.koreatech.koin.domain.user.service;

import static in.koreatech.koin.domain.user.model.UserType.*;

import java.time.LocalDateTime;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin._common.auth.JwtProvider;
import in.koreatech.koin._common.auth.exception.AuthenticationException;
import in.koreatech.koin._common.event.UserDeleteEvent;
import in.koreatech.koin._common.event.UserRegisterEvent;
import in.koreatech.koin.domain.owner.repository.OwnerRepository;
import in.koreatech.koin.domain.student.repository.StudentRepository;
import in.koreatech.koin.domain.timetableV2.repository.TimetableFrameRepositoryV2;
import in.koreatech.koin.domain.user.dto.UserFindIdByEmailRequest;
import in.koreatech.koin.domain.user.dto.UserFindIdBySmsRequest;
import in.koreatech.koin.domain.user.dto.UserFindLoginIdResponse;
import in.koreatech.koin.domain.user.dto.UserLoginRequest;
import in.koreatech.koin.domain.user.dto.UserLoginRequestV2;
import in.koreatech.koin.domain.user.dto.UserLoginResponse;
import in.koreatech.koin.domain.user.dto.UserRefreshTokenRequest;
import in.koreatech.koin.domain.user.dto.UserRefreshTokenResponse;
import in.koreatech.koin.domain.user.dto.UserRegisterRequest;
import in.koreatech.koin.domain.user.dto.UserResetPasswordByEmailRequest;
import in.koreatech.koin.domain.user.dto.UserResetPasswordBySmsRequest;
import in.koreatech.koin.domain.user.dto.UserResponse;
import in.koreatech.koin.domain.user.dto.UserTypeResponse;
import in.koreatech.koin.domain.user.dto.UserUpdateRequest;
import in.koreatech.koin.domain.user.dto.UserUpdateResponse;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.model.UserToken;
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

    public UserResponse getUser(Integer userId) {
        User user = userRepository.getById(userId);
        return UserResponse.from(user);
    }

    public UserTypeResponse getUserType(Integer userId) {
        User user = userRepository.getById(userId);
        return UserTypeResponse.from(user);
    }

    @Transactional
    public void registerUser(UserRegisterRequest request) {
        userValidationService.requireUniqueRegister(
            request.nickname(),
            request.phoneNumber(),
            request.email(),
            request.loginId()
        );
        User user = request.toUser(passwordEncoder);
        userRepository.save(user);
        eventPublisher.publishEvent(new UserRegisterEvent(user.getId(), request.marketingNotificationAgreement()));
        userVerificationService.consumeVerification(request.phoneNumber());
    }

    @Transactional
    public UserUpdateResponse updateUser(Integer userId, UserUpdateRequest request) {
        User user = userRepository.getById(userId);
        userValidationService.requireUniqueUpdate(
            request.nickname(),
            request.phoneNumber(),
            request.email(),
            user
        );
        user.update(request.email(), request.nickname(), request.name(), request.phoneNumber(), request.gender());
        user.updatePassword(passwordEncoder, request.password());
        return UserUpdateResponse.from(user);
    }

    @Transactional
    public UserLoginResponse loginV2(UserLoginRequestV2 request) {
        User user;
        String loginId = request.loginId();
        if (loginId.matches("^\\d{11}$")) {
            user = userRepository.getByPhoneNumber(loginId);
        } else {
            user = userRepository.getByUserId(loginId);
        }
        user.requireSameLoginPw(passwordEncoder, request.loginPw());
        return createLoginResponse(user);
    }

    @Transactional
    public UserLoginResponse login(UserLoginRequest request) {
        User user = userRepository.getByEmail(request.email());
        user.requireSameLoginPw(passwordEncoder, request.password());
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

    public UserRefreshTokenResponse refresh(UserRefreshTokenRequest request) {
        Integer userId = refreshTokenService.extractUserId(request.refreshToken());
        UserToken userToken = refreshTokenService.verifyAndGetUserToken(request.refreshToken(), userId);

        User user = userRepository.findById(userToken.getId())
            .orElseThrow(() -> AuthenticationException.withDetail("유효하지 않은 토큰입니다. userId : " + userId));

        String accessToken = jwtProvider.createToken(user);
        return UserRefreshTokenResponse.of(accessToken, userToken.getRefreshToken());
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

    public UserFindLoginIdResponse findIdBySms(UserFindIdBySmsRequest request) {
        String phoneNumber = request.phoneNumber();
        User user = userRepository.getByPhoneNumberAndUserTypeIn(phoneNumber, KOIN_USER_TYPES);
        userVerificationService.consumeVerification(phoneNumber);
        return UserFindLoginIdResponse.from(user.getLoginId());
    }

    public UserFindLoginIdResponse findIdByEmail(UserFindIdByEmailRequest request) {
        String email = request.email();
        User user = userRepository.getByEmailAndUserTypeIn(email, KOIN_USER_TYPES);
        userVerificationService.consumeVerification(email);
        return UserFindLoginIdResponse.from(user.getLoginId());
    }

    @Transactional
    public void resetPasswordBySms(UserResetPasswordBySmsRequest request) {
        User user = userRepository.getByLoginIdAndUserTypeIn(request.loginId(), KOIN_USER_TYPES);
        user.requireSamePhoneNumber(request.phoneNumber());
        user.updatePassword(passwordEncoder, request.newPassword());
        userRepository.save(user);
        userVerificationService.consumeVerification(request.phoneNumber());
    }

    @Transactional
    public void resetPasswordByEmail(UserResetPasswordByEmailRequest request) {
        User user = userRepository.getByLoginIdAndUserTypeIn(request.loginId(), KOIN_USER_TYPES);
        user.requireSameEmail(request.email());
        user.updatePassword(passwordEncoder, request.newPassword());
        userRepository.save(user);
        userVerificationService.consumeVerification(request.email());
    }
}
