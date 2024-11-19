package in.koreatech.koin.domain.user.service;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import in.koreatech.koin.domain.student.model.redis.StudentTemporaryStatus;
import in.koreatech.koin.domain.student.repository.StudentRedisRepository;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
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
import in.koreatech.koin.domain.user.repository.UserTokenRepository;
import in.koreatech.koin.global.auth.JwtProvider;
import in.koreatech.koin.global.auth.exception.AuthorizationException;
import in.koreatech.koin.global.exception.KoinIllegalArgumentException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final JwtProvider jwtProvider;
    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final StudentRedisRepository studentRedisRepository;
    private final OwnerRepository ownerRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserTokenRepository userTokenRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final TimetableFrameRepositoryV2 timetableFrameRepositoryV2;

    @Transactional
    public UserLoginResponse login(UserLoginRequest request) {
        User user = checkLoginCredentials(request.email(), request.password());
        checkUserAuthentication(request.email());

        String accessToken = jwtProvider.createToken(user);
        String refreshToken = generateRefreshToken(user);
        UserToken savedToken = userTokenRepository.save(UserToken.create(user.getId(), refreshToken));

        updateLastLoginTime(user);
        return UserLoginResponse.of(accessToken, savedToken.getRefreshToken(), user.getUserType().getValue());
    }

    @Transactional
    public void logout(Integer userId) {
        userTokenRepository.deleteById(userId);
    }

    public void checkLogin(String accessToken) {
        jwtProvider.getUserId(accessToken);
    }

    public UserTokenRefreshResponse refresh(UserTokenRefreshRequest request) {
        String userId = getUserId(request.refreshToken());
        UserToken userToken = userTokenRepository.getById(Integer.parseInt(userId));
        if (!Objects.equals(userToken.getRefreshToken(), request.refreshToken())) {
            throw new KoinIllegalArgumentException("refresh token이 일치하지 않습니다.", "request: " + request);
        }
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

    public AuthResponse getAuth(Integer userId) {
        User user = userRepository.getById(userId);
        return AuthResponse.from(user);
    }

    public CoopResponse getCoop(Integer userId) {
        User user = userRepository.getById(userId);
        return CoopResponse.from(user);
    }

    private String getUserId(String refreshToken) {
        String[] split = refreshToken.split("-");
        if (split.length == 0) {
            throw new AuthorizationException("올바르지 않은 인증 토큰입니다. refreshToken: " + refreshToken);
        }
        return split[split.length - 1];
    }

    public User checkLoginCredentials(String email, String password) {
        User user = userRepository.getByEmail(email);
        if (!user.isSamePassword(passwordEncoder, password)) {
            throw new KoinIllegalArgumentException("비밀번호가 틀렸습니다.");
        }
        return user;
    }

    public void checkUserAuthentication(String email) {
        Optional<StudentTemporaryStatus> studentTemporaryStatus = studentRedisRepository.findById(email);
        if (studentTemporaryStatus.isPresent()) {
            throw new AuthorizationException("미인증 상태입니다. 아우누리에서 인증메일을 확인해주세요");
        }
    }

    public String generateRefreshToken(User user) {
        return String.format("%s-%d", UUID.randomUUID(), user.getId());
    }

    public void updateLastLoginTime(User user) {
        user.updateLastLoggedTime(LocalDateTime.now());
        userRepository.save(user);
    }
}
