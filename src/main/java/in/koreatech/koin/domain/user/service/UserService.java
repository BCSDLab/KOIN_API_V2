package in.koreatech.koin.domain.user.service;

import static in.koreatech.koin.domain.user.model.UserType.*;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin._common.auth.JwtProvider;
import in.koreatech.koin._common.event.UserDeleteEvent;
import in.koreatech.koin._common.event.UserRegisterEvent;
import in.koreatech.koin._common.exception.custom.KoinIllegalArgumentException;
import in.koreatech.koin.admin.abtest.useragent.UserAgentInfo;
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
import in.koreatech.koin.domain.user.repository.UserRepository;
import in.koreatech.koin.domain.user.verification.service.UserVerificationService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final OwnerRepository ownerRepository;
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
        user.updatePassword(passwordEncoder, request.password());
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
        eventPublisher.publishEvent(new UserRegisterEvent(user.getId(), request.marketingNotificationAgreement()));
        userVerificationService.consumeVerification(request.phoneNumber());
    }

    @Transactional
    public UserLoginResponse loginV2(UserLoginRequestV2 request, UserAgentInfo userAgentInfo) {
        User user = userValidationService.checkLoginCredentialsV2(request.loginId(), request.loginPw());
        return createLoginResponse(user, userAgentInfo);
    }

    @Transactional
    public UserLoginResponse login(UserLoginRequest request, UserAgentInfo userAgentInfo) {
        User user = userValidationService.checkLoginCredentials(request.email(), request.password());
        userValidationService.checkUserAuthentication(request.email());
        return createLoginResponse(user, userAgentInfo);
    }

    private UserLoginResponse createLoginResponse(User user, UserAgentInfo userAgentInfo) {
        String accessToken = jwtProvider.createToken(user);
        String refreshToken = refreshTokenService.createRefreshToken(user.getId(), userAgentInfo.getType());
        user.updateLastLoggedTime(LocalDateTime.now());
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

    public RefreshUserTokenResponse refresh(RefreshUserTokenRequest request, UserAgentInfo userAgentInfo) {
        Integer userId = refreshTokenService.extractUserId(request.refreshToken());
        refreshTokenService.verifyRefreshToken(userId, userAgentInfo.getType(), request.refreshToken());
        User user = userRepository.getById(userId);
        String accessToken = jwtProvider.createToken(user);
        return RefreshUserTokenResponse.of(accessToken, refreshTokenService.getRefreshToken(userId, userAgentInfo.getType()));
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
        refreshTokenService.deleteAllRefreshTokens(user.getId());
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
