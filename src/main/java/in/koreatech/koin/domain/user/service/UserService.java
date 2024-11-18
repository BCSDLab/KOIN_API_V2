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
import in.koreatech.koin.domain.user.dto.EmailCheckExistsRequest;
import in.koreatech.koin.domain.user.dto.NicknameCheckExistsRequest;
import in.koreatech.koin.domain.user.dto.UserLoginRequest;
import in.koreatech.koin.domain.user.dto.UserLoginResponse;
import in.koreatech.koin.domain.user.dto.UserPasswordCheckRequest;
import in.koreatech.koin.domain.user.dto.UserTokenRefreshRequest;
import in.koreatech.koin.domain.user.dto.UserTokenRefreshResponse;
import in.koreatech.koin.domain.user.exception.DuplicationNicknameException;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.model.UserDeleteEvent;
import in.koreatech.koin.domain.user.model.UserToken;
import in.koreatech.koin.domain.user.model.UserType;
import in.koreatech.koin.domain.student.repository.StudentRepository;
import in.koreatech.koin.domain.user.repository.UserRepository;
import in.koreatech.koin.domain.user.repository.UserTokenRepository;
import in.koreatech.koin.global.auth.JwtProvider;
import in.koreatech.koin.global.auth.exception.AuthorizationException;
import in.koreatech.koin.global.domain.email.exception.DuplicationEmailException;
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
        User user = userRepository.getByEmail(request.email());
        Optional<StudentTemporaryStatus> studentTemporaryStatus = studentRedisRepository.findById(request.email());

        if (!user.isSamePassword(passwordEncoder, request.password())) {
            throw new KoinIllegalArgumentException("비밀번호가 틀렸습니다.");
        }

        if (studentTemporaryStatus.isPresent()) {
            throw new AuthorizationException("미인증 상태입니다. 아우누리에서 인증메일을 확인해주세요");
        }

        String accessToken = jwtProvider.createToken(user);
        String refreshToken = String.format("%s-%d", UUID.randomUUID(), user.getId());
        UserToken savedToken = userTokenRepository.save(UserToken.create(user.getId(), refreshToken));
        user.updateLastLoggedTime(LocalDateTime.now());
        User saved = userRepository.save(user);

        return UserLoginResponse.of(accessToken, savedToken.getRefreshToken(), saved.getUserType().getValue());
    }

    @Transactional
    public void logout(Integer userId) {
        userTokenRepository.deleteById(userId);
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

    private String getUserId(String refreshToken) {
        String[] split = refreshToken.split("-");
        if (split.length == 0) {
            throw new AuthorizationException("올바르지 않은 인증 토큰입니다. refreshToken: " + refreshToken);
        }
        return split[split.length - 1];
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

    public void checkLogin(String accessToken) {
        jwtProvider.getUserId(accessToken);
    }

    public void checkPassword(UserPasswordCheckRequest request, Integer userId) {
        User user = userRepository.getById(userId);
        if (!user.isSamePassword(passwordEncoder, request.password())) {
            throw new KoinIllegalArgumentException("올바르지 않은 비밀번호입니다.");
        }
    }

    public void checkExistsEmail(EmailCheckExistsRequest request) {
        userRepository.findByEmail(request.email()).ifPresent(user -> {
            throw DuplicationEmailException.withDetail("account: " + user.getEmail());
        });
    }

    public void checkUserNickname(NicknameCheckExistsRequest request) {
        userRepository.findByNickname(request.nickname()).ifPresent(user -> {
            throw DuplicationNicknameException.withDetail("nickname: " + request.nickname());
        });
    }

    public AuthResponse getAuth(Integer userId) {
        User user = userRepository.getById(userId);
        return AuthResponse.from(user);
    }

    public CoopResponse getCoop(Integer userId) {
        User user = userRepository.getById(userId);
        return CoopResponse.from(user);
    }
}
