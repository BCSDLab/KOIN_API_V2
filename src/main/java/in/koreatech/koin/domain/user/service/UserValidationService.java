package in.koreatech.koin.domain.user.service;

import static in.koreatech.koin.domain.user.model.UserType.GENERAL;
import static in.koreatech.koin.domain.user.model.UserType.STUDENT;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin._common.auth.exception.AuthorizationException;
import in.koreatech.koin._common.exception.custom.KoinIllegalArgumentException;
import in.koreatech.koin.domain.student.model.redis.UnAuthenticatedStudentInfo;
import in.koreatech.koin.domain.student.repository.StudentRedisRepository;
import in.koreatech.koin.domain.user.exception.DuplicationLoginIdException;
import in.koreatech.koin.domain.user.exception.DuplicationNicknameException;
import in.koreatech.koin.domain.user.exception.DuplicationPhoneNumberException;
import in.koreatech.koin.domain.user.exception.UserNotFoundException;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.repository.UserRepository;
import in.koreatech.koin.domain.user.verification.service.UserVerificationService;
import in.koreatech.koin.infrastructure.email.exception.DuplicationEmailException;
import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserValidationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final StudentRedisRepository studentRedisRepository;
    private final UserVerificationService userVerificationService;

    public void checkPassword(String password, Integer userId) {
        User user = userRepository.getById(userId);
        user.validatePassword(passwordEncoder, password);
    }

    public void checkDuplicatedEmail(String email) {
        if (StringUtils.isNotBlank(email) && userRepository.existsByEmail(email)) {
            throw DuplicationEmailException.withDetail("email: " + email);
        }
    }

    public void checkDuplicatedPhoneNumber(String phoneNumber) {
        if (StringUtils.isNotBlank(phoneNumber) &&
            userRepository.existsByPhoneNumberAndUserTypeIn(phoneNumber, List.of(GENERAL, STUDENT))) {
            throw DuplicationPhoneNumberException.withDetail("phoneNumber: " + phoneNumber);
        }
    }

    public void checkDuplicatedNickname(String nickname) {
        if (StringUtils.isNotBlank(nickname) && userRepository.existsByNickname(nickname)) {
            throw DuplicationNicknameException.withDetail("nickname: " + nickname);
        }
    }

    public void checkDuplicatedLoginId(String loginId) {
        if (userRepository.existsByUserId(loginId)) {
            throw DuplicationLoginIdException.withDetail("loginId: " + loginId);
        }
    }

    public void checkDuplicatedUpdateNickname(String updateNickname, Integer userId) {
        User user = userRepository.getById(userId);
        if (updateNickname != null && !updateNickname.equals(user.getNickname())
            && userRepository.existsByNickname(updateNickname)) {
            throw DuplicationNicknameException.withDetail("updateNickname : " + updateNickname);
        }
    }

    public void checkDuplicatedUpdateEmail(String updateEmail, Integer userId) {
        User user = userRepository.getById(userId);
        if (updateEmail != null && !updateEmail.equals(user.getEmail())
            && userRepository.existsByEmail(updateEmail)) {
            throw DuplicationEmailException.withDetail("updateEmail : " + updateEmail);
        }
    }

    public void checkDuplicatedUpdatePhoneNumber(String updatePhoneNumber, Integer userId) {
        User user = userRepository.getById(userId);
        if (user.isNotSamePhoneNumber(updatePhoneNumber)) {
            checkDuplicatedPhoneNumber(updatePhoneNumber);
            userVerificationService.consumeVerification(updatePhoneNumber);
        }
    }

    public void checkDuplicationUserData(String nickname, String email, String phoneNumber, String loginId) {
        checkDuplicatedNickname(nickname);
        checkDuplicatedEmail(email);
        checkDuplicatedPhoneNumber(phoneNumber);
        checkDuplicatedLoginId(loginId);
    }

    public User checkLoginCredentials(String email, String password) {
        User user = userRepository.getByEmail(email);
        user.validatePassword(passwordEncoder, password);
        return user;
    }

    public User checkLoginCredentialsV2(String loginId, String loginPw) {
        User user;
        if (loginId.matches("^\\d{11}$")) {
            user = userRepository.getByPhoneNumber(loginId);
        } else {
            user = userRepository.getByUserId(loginId);
        }
        user.validatePassword(passwordEncoder, loginPw);
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
        if (userRepository.existsByPhoneNumberAndUserTypeIn(phoneNumber, List.of(GENERAL, STUDENT))) {
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
