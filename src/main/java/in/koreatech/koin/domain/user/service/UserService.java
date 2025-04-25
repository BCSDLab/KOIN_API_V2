package in.koreatech.koin.domain.user.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin._common.auth.JwtProvider;
import in.koreatech.koin._common.event.UserDeleteEvent;
import in.koreatech.koin._common.exception.custom.KoinIllegalArgumentException;
import in.koreatech.koin.domain.owner.repository.OwnerRepository;
import in.koreatech.koin.domain.student.repository.StudentRepository;
import in.koreatech.koin.domain.timetableV2.repository.TimetableFrameRepositoryV2;
import in.koreatech.koin.domain.user.dto.AuthResponse;
import in.koreatech.koin.domain.user.dto.GeneralUserRegisterRequest;
import in.koreatech.koin.domain.user.dto.UserLoginRequest;
import in.koreatech.koin.domain.user.dto.UserLoginRequestV2;
import in.koreatech.koin.domain.user.dto.UserLoginResponse;
import in.koreatech.koin.domain.user.dto.UserTokenRefreshRequest;
import in.koreatech.koin.domain.user.dto.UserTokenRefreshResponse;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.model.UserToken;
import in.koreatech.koin.domain.user.model.UserType;
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
    private final UserVerificationService userVerificationService;
    private final TimetableFrameRepositoryV2 timetableFrameRepositoryV2;
    private final ApplicationEventPublisher eventPublisher;
    private final UserValidationService userValidationService;
    private final RefreshTokenService refreshTokenService;
    private final JwtProvider jwtProvider;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void generalUserRegister(GeneralUserRegisterRequest request) {
        userVerificationService.checkVerified(request.phoneNumber());
        User user = request.toUser(passwordEncoder);
        userRepository.save(user);
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

    public UserTokenRefreshResponse refresh(UserTokenRefreshRequest request) {
        String userId = refreshTokenService.extractUserId(request.refreshToken());
        UserToken userToken = refreshTokenService.verifyAndGetUserToken(request.refreshToken(),
            Integer.parseInt(userId));

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

    public String findIdBySms(String phoneNumber) {
        userVerificationService.checkVerified(phoneNumber);
        User user = userRepository.getByPhoneNumberAndUserTypeIn(phoneNumber, List.of(UserType.GENERAL, UserType.STUDENT));
        return user.getUserId();
    }

    public String findIdByEmail(String email) {
        userVerificationService.checkVerified(email);
        User user = userRepository.getByEmailAndUserTypeIn(email, List.of(UserType.GENERAL, UserType.STUDENT));
        return user.getUserId();
    }

    @Transactional
    public void resetPasswordBySms(String userId, String phoneNumber, String newPassword) {
        userVerificationService.checkVerified(phoneNumber);
        User user = userRepository.getByUserIdAndUserTypeIn(userId, List.of(UserType.GENERAL, UserType.STUDENT));
        if (Objects.equals(user.getPhoneNumber(), phoneNumber)) {
            user.updatePassword(passwordEncoder, newPassword);
            userRepository.save(user);
            return;
        }
        throw new KoinIllegalArgumentException("입력한 아이디와 인증된 사용자 정보가 일치하지 않습니다.");
    }

    @Transactional
    public void resetPasswordByEmail(String userId, String email, String newPassword) {
        userVerificationService.checkVerified(email);
        User user = userRepository.getByUserIdAndUserTypeIn(userId, List.of(UserType.GENERAL, UserType.STUDENT));
        if (Objects.equals(user.getEmail(), email)) {
            user.updatePassword(passwordEncoder, newPassword);
            userRepository.save(user);
            return;
        }
        throw new KoinIllegalArgumentException("입력한 아이디와 인증된 사용자 정보가 일치하지 않습니다.");
    }
}
