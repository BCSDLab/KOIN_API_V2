package in.koreatech.koin.domain.user.service;

import java.util.Objects;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.student.model.redis.UnAuthenticatedStudentInfo;
import in.koreatech.koin.domain.student.repository.StudentRedisRepository;
import in.koreatech.koin.domain.user.dto.EmailCheckExistsRequest;
import in.koreatech.koin.domain.user.dto.NicknameCheckExistsRequest;
import in.koreatech.koin.domain.user.dto.PhoneCheckExistsRequest;
import in.koreatech.koin.domain.user.dto.UserPasswordCheckRequest;
import in.koreatech.koin.domain.user.exception.DuplicationNicknameException;
import in.koreatech.koin.domain.user.exception.DuplicationPhoneNumberException;
import in.koreatech.koin.domain.user.exception.UserNotFoundException;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.repository.UserRepository;
import in.koreatech.koin._common.auth.exception.AuthorizationException;
import in.koreatech.koin.integration.email.exception.DuplicationEmailException;
import in.koreatech.koin._common.exception.custom.KoinIllegalArgumentException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserValidationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final StudentRedisRepository studentRedisRepository;

    public void checkPassword(UserPasswordCheckRequest request, Integer userId) {
        User user = userRepository.getById(userId);
        if (!user.isSamePassword(passwordEncoder, request.password())) {
            throw new KoinIllegalArgumentException("올바르지 않은 비밀번호입니다.");
        }
    }

    public void checkExistsEmail(EmailCheckExistsRequest request) {
        userRepository.findByEmail(request.email()).ifPresent(user -> {
            throw DuplicationEmailException.withDetail("email: " + user.getEmail());
        });
    }

    public void checkExistsPhoneNumber(PhoneCheckExistsRequest request) {
        userRepository.findByPhoneNumber(request.phone()).ifPresent(user -> {
            throw DuplicationPhoneNumberException.withDetail("phone: " + user.getPhoneNumber());
        });
    }

    public void checkUserNickname(NicknameCheckExistsRequest request) {
        userRepository.findByNickname(request.nickname()).ifPresent(user -> {
            throw DuplicationNicknameException.withDetail("nickname: " + request.nickname());
        });
    }

    public User checkLoginCredentials(String email, String password) {
        User user = userRepository.getByEmail(email);
        if (!user.isSamePassword(passwordEncoder, password)) {
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

    @Transactional(readOnly = true)
    public void existsByUserId(String userId) {
        if (!userRepository.existsByUserId(userId)) {
            throw UserNotFoundException.withDetail("userId: " + userId);
        }
    }

    @Transactional(readOnly = true)
    public void existsByPhoneNumber(String phoneNumber) {
        if (!userRepository.existsByPhoneNumber(phoneNumber)) {
            throw UserNotFoundException.withDetail("userId: " + phoneNumber);
        }
    }

    @Transactional(readOnly = true)
    public void existsByEmail(String email) {
        if (!userRepository.existsByEmail(email)) {
            throw UserNotFoundException.withDetail("userId: " + email);
        }
    }

    @Transactional(readOnly = true)
    public void existsByUserIdAndPhoneNumber(String userId, String phoneNumber) {
        User user = userRepository.getByUserId(userId);
        if(!Objects.equals(user.getPhoneNumber(), phoneNumber)) {
            throw new KoinIllegalArgumentException("입력한 아이디의 휴대폰 번호와 일치하지 않습니다.");
        }
    }

    @Transactional(readOnly = true)
    public void existsByUserIdAndEmail(String userId, String email) {
        User user = userRepository.getByUserId(userId);
        if(!Objects.equals(user.getEmail(), email)) {
            throw new KoinIllegalArgumentException("입력한 아이디의 이메일과 일치하지 않습니다.");
        }
    }
}
