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
import in.koreatech.koin.domain.user.dto.UserLoginResponse;
import in.koreatech.koin.domain.user.dto.UserTokenRefreshRequest;
import in.koreatech.koin.domain.user.dto.UserTokenRefreshResponse;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.model.UserToken;
import in.koreatech.koin.domain.user.model.UserType;
import in.koreatech.koin.domain.user.model.UserVerificationStatus;
import in.koreatech.koin.domain.user.model.VerificationType;
import in.koreatech.koin.domain.user.repository.UserRepository;
import in.koreatech.koin.domain.user.repository.UserTokenRedisRepository;
import in.koreatech.koin.domain.user.repository.UserVerificationStatusRedisRepository;
import in.koreatech.koin.domain.user.service.verification.VerificationTypeDetector;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final OwnerRepository ownerRepository;
    private final UserTokenRedisRepository userTokenRedisRepository;
    private final UserVerificationStatusRedisRepository userVerificationStatusRedisRepository;
    private final TimetableFrameRepositoryV2 timetableFrameRepositoryV2;
    private final ApplicationEventPublisher eventPublisher;
    private final UserValidationService userValidationService;
    private final RefreshTokenService refreshTokenService;
    private final JwtProvider jwtProvider;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void generalUserRegister(GeneralUserRegisterRequest request) {
        checkVerified(request.phoneNumber());
        User user = request.toUser(passwordEncoder);
        userRepository.save(user);
        userVerificationStatusRedisRepository.deleteById(request.phoneNumber());
    }

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

    @Transactional
    public String findIdByVerification(String target) {
        checkVerified(target);
        User user = findUserByVerification(target);
        userVerificationStatusRedisRepository.deleteById(target);
        return user.getUserId();
    }

    @Transactional
    public void resetPasswordByVerification(String userId, String target, String newPassword) {
        checkVerified(target);
        User user = findUserByVerification(target);
        // SMS 인증인 경우만 사용자 ID 일치 여부 검사
        boolean isSmsVerification = VerificationTypeDetector.detect(target) == VerificationType.SMS;
        boolean isUserIdMismatch = !Objects.equals(user.getUserId(), userId);
        if (isSmsVerification && isUserIdMismatch) {
            throw new KoinIllegalArgumentException("입력한 아이디와 인증된 사용자 정보가 일치하지 않습니다.");
        }
        user.updatePassword(passwordEncoder, newPassword);
        userRepository.save(user);
        userVerificationStatusRedisRepository.deleteById(target);
    }

    private void checkVerified(String target) {
        UserVerificationStatus userVerificationStatus = userVerificationStatusRedisRepository.getById(target);
        if (!userVerificationStatus.isVerified()) {
            throw new KoinIllegalArgumentException("유효하지 않은 인증 정보입니다.");
        }
    }

    private User findUserByVerification(String target) {
        VerificationType verificationType = VerificationTypeDetector.detect(target);
        if (verificationType == VerificationType.EMAIL) {
            return userRepository.getByEmailAndUserTypeIn(target, List.of(UserType.GENERAL, UserType.STUDENT));
        } else if (verificationType == VerificationType.SMS) {
            return userRepository.getByPhoneNumberAndUserTypeIn(target,
                List.of(UserType.GENERAL, UserType.STUDENT));
        } else {
            throw new KoinIllegalArgumentException("유효하지 않은 인증 정보입니다.");
        }
    }
}
