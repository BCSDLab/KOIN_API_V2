package in.koreatech.koin.domain.user.service;

import java.util.Objects;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin._common.auth.exception.AuthorizationException;
import in.koreatech.koin._common.exception.custom.KoinIllegalArgumentException;
import in.koreatech.koin.domain.student.model.redis.UnAuthenticatedStudentInfo;
import in.koreatech.koin.domain.student.repository.StudentRedisRepository;
import in.koreatech.koin.domain.user.dto.validation.CheckEmailDuplicationRequest;
import in.koreatech.koin.domain.user.dto.validation.CheckLoginIdDuplicationRequest;
import in.koreatech.koin.domain.user.dto.validation.CheckNicknameDuplicationRequest;
import in.koreatech.koin.domain.user.dto.validation.CheckPhoneDuplicationRequest;
import in.koreatech.koin.domain.user.dto.validation.CheckUserPasswordRequest;
import in.koreatech.koin.domain.user.exception.DuplicationLoginIdException;
import in.koreatech.koin.domain.user.exception.DuplicationNicknameException;
import in.koreatech.koin.domain.user.exception.DuplicationPhoneNumberException;
import in.koreatech.koin.domain.user.exception.UserNotFoundException;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.repository.UserRepository;
import in.koreatech.koin.integration.email.exception.DuplicationEmailException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserValidationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final StudentRedisRepository studentRedisRepository;

    public void checkPassword(CheckUserPasswordRequest request, Integer userId) {
        User user = userRepository.getById(userId);
        if (!user.isSamePassword(passwordEncoder, request.password())) {
            throw new KoinIllegalArgumentException("올바르지 않은 비밀번호입니다.");
        }
    }

    public void checkDuplicatedEmail(CheckEmailDuplicationRequest request) {
        userRepository.findByEmail(request.email()).ifPresent(user -> {
            throw DuplicationEmailException.withDetail("email: " + user.getEmail());
        });
    }

    public void checkDuplicatedPhoneNumber(CheckPhoneDuplicationRequest request) {
        userRepository.findByPhoneNumber(request.phone()).ifPresent(user -> {
            throw DuplicationPhoneNumberException.withDetail("phone: " + user.getPhoneNumber());
        });
    }

    public void checkDuplicatedNickname(CheckNicknameDuplicationRequest request) {
        userRepository.findByNickname(request.nickname()).ifPresent(user -> {
            throw DuplicationNicknameException.withDetail("nickname: " + request.nickname());
        });
    }

    public void checkDuplicatedLoginId(CheckLoginIdDuplicationRequest request) {
        userRepository.findByUserId(request.loginId()).ifPresent(user -> {
            throw DuplicationLoginIdException.withDetail("loginId: " + request.loginId());
        });
    }

    public User checkLoginCredentials(String email, String password) {
        User user = userRepository.getByEmail(email);
        if (!user.isSamePassword(passwordEncoder, password)) {
            throw new KoinIllegalArgumentException("비밀번호가 틀렸습니다.");
        }
        return user;
    }

    public User checkLoginCredentialsV2(String loginId, String loginPw) {
        User user;
        if (loginId.matches("^\\d{11}$")) {
            user = userRepository.getByPhoneNumber(loginId);
        } else {
            user = userRepository.getByUserId(loginId);
        }
        if (!user.isSamePassword(passwordEncoder, loginPw)) {
            throw new KoinIllegalArgumentException("비밀번호가 틀렸습니다.");
        }
        return user;
    }

    public void checkUserAuthentication(String email) {
        Optional<UnAuthenticatedStudentInfo> studentTemporaryStatus = studentRedisRepository.findById(email);
        if (studentTemporaryStatus.isPresent()) {
            throw new AuthorizationException("미인증 상태입니다. 아우누리에서 인증메일을 확인해주세요");
        }
    }

    public void existsByUserId(String loginId) {
        if (userRepository.existsByUserId(loginId)) {
            return;
        }
        throw UserNotFoundException.withDetail("loginId: " + loginId);
    }

    public void existsByPhoneNumber(String phoneNumber) {
        if (userRepository.existsByPhoneNumber(phoneNumber)) {
            return;
        }
        throw UserNotFoundException.withDetail("phoneNumber: " + phoneNumber);
    }

    public void existsByEmail(String email) {
        if (userRepository.existsByEmail(email)) {
            return;
        }
        throw UserNotFoundException.withDetail("email: " + email);
    }

    public void matchUserIdWithPhoneNumber(String userId, String phoneNumber) {
        User user = userRepository.getByUserId(userId);
        if (Objects.equals(user.getPhoneNumber(), phoneNumber)) {
            return;
        }
        throw new KoinIllegalArgumentException("입력한 아이디의 휴대폰 번호와 일치하지 않습니다.");
    }

    public void matchUserIdWithEmail(String userId, String email) {
        User user = userRepository.getByUserId(userId);
        if (Objects.equals(user.getEmail(), email)) {
            return;
        }
        throw new KoinIllegalArgumentException("입력한 아이디의 이메일과 일치하지 않습니다.");
    }
}
