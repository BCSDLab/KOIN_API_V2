package in.koreatech.koin.domain.user.service;

import static in.koreatech.koin.domain.user.model.UserType.KOIN_USER_TYPES;

import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import in.koreatech.koin._common.auth.JwtProvider;
import in.koreatech.koin._common.auth.exception.AuthorizationException;
import in.koreatech.koin.domain.student.model.redis.UnAuthenticatedStudentInfo;
import in.koreatech.koin.domain.student.repository.StudentRedisRepository;
import in.koreatech.koin.domain.user.dto.validation.UserMatchLoginIdWithEmailRequest;
import in.koreatech.koin.domain.user.dto.validation.UserMatchLoginIdWithPhoneNumberRequest;
import in.koreatech.koin.domain.user.exception.DuplicationLoginIdException;
import in.koreatech.koin.domain.user.exception.DuplicationNicknameException;
import in.koreatech.koin.domain.user.exception.DuplicationPhoneNumberException;
import in.koreatech.koin.domain.user.exception.UserNotFoundException;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.repository.UserRepository;
import in.koreatech.koin.domain.user.verification.service.UserVerificationService;
import in.koreatech.koin.infrastructure.email.exception.DuplicationEmailException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserValidationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final StudentRedisRepository studentRedisRepository;
    private final UserVerificationService userVerificationService;
    private final JwtProvider jwtProvider;

    public void requireLogin(String accessToken) {
        jwtProvider.getUserId(accessToken);
    }

    public void requirePasswordCorrect(String password, Integer userId) {
        User user = userRepository.getById(userId);
        user.requireSameLoginPw(passwordEncoder, password);
    }

    public void requireUniqueNickname(String nickname) {
        if (StringUtils.hasText(nickname)
            && userRepository.existsByNicknameAndUserTypeIn(nickname, KOIN_USER_TYPES)) {
            throw DuplicationNicknameException.withDetail("nickname: " + nickname);
        }
    }

    public void requireUniquePhoneNumber(String phoneNumber) {
        System.out.println("########### phoneNumber = " + phoneNumber);
        if (StringUtils.hasText(phoneNumber)
            && userRepository.existsByPhoneNumberAndUserTypeIn(phoneNumber, KOIN_USER_TYPES)) {
            throw DuplicationPhoneNumberException.withDetail("phoneNumber: " + phoneNumber);
        }
    }

    public void requireUniqueEmail(String email) {
        if (StringUtils.hasText(email)
            && userRepository.existsByEmailAndUserTypeIn(email, KOIN_USER_TYPES)) {
            throw DuplicationEmailException.withDetail("email: " + email);
        }
    }

    public void requireUniqueLoginId(String loginId) {
        if (StringUtils.hasText(loginId)
            && userRepository.existsByLoginIdAndUserTypeIn(loginId, KOIN_USER_TYPES)) {
            throw DuplicationLoginIdException.withDetail("loginId: " + loginId);
        }
    }

    public void checkUserAuthentication(String email) {
        Optional<UnAuthenticatedStudentInfo> studentTemporaryStatus = studentRedisRepository.findById(email);
        if (studentTemporaryStatus.isPresent()) {
            throw new AuthorizationException("미인증 상태입니다. 아우누리에서 인증메일을 확인해주세요");
        }
    }

    public void requireLoginIdExists(String loginId) {
        if (userRepository.existsByLoginIdAndUserTypeIn(loginId, KOIN_USER_TYPES)) {
            return;
        }
        throw UserNotFoundException.withDetail("loginId: " + loginId);
    }

    public void requirePhoneNumberExists(String phoneNumber) {
        if (userRepository.existsByPhoneNumberAndUserTypeIn(phoneNumber, KOIN_USER_TYPES)) {
            return;
        }
        throw UserNotFoundException.withDetail("phoneNumber: " + phoneNumber);
    }

    public void requireEmailExists(String email) {
        if (userRepository.existsByEmailAndUserTypeIn(email, KOIN_USER_TYPES)) {
            return;
        }
        throw UserNotFoundException.withDetail("email: " + email);
    }

    public void matchLoginIdWithPhoneNumber(UserMatchLoginIdWithPhoneNumberRequest request) {
        User user = userRepository.getByUserId(request.loginId());
        user.requireSamePhoneNumber(request.phoneNumber());
    }

    public void matchLoginIdWithEmail(UserMatchLoginIdWithEmailRequest request) {
        User user = userRepository.getByUserId(request.loginId());
        user.requireSameEmail(request.email());
    }

    public void requireUniqueRegister(String nickname, String phoneNumber, String email, String loginId) {
        requireUniqueNickname(nickname);
        requireUniquePhoneNumber(phoneNumber);
        requireUniqueEmail(email);
        requireUniqueLoginId(loginId);
    }

    public void requireNicknameUniqueUpdate(String newNickname, User user) {
        if (user.isNotSameNickname(newNickname)) {
            requireUniqueNickname(newNickname);
        }
    }

    @Transactional
    public void requireUniquePhoneNumberUpdate(String newPhoneNumber, User user) {
        if (user.isNotSamePhoneNumber(newPhoneNumber)) {
            requireUniquePhoneNumber(newPhoneNumber);
            userVerificationService.consumeVerification(newPhoneNumber);
        }
    }

    public void requireUniqueEmailUpdate(String newEmail, User user) {
        if (user.isNotSameEmail(newEmail)) {
            requireUniqueEmail(newEmail);
        }
    }

    public void requireUniqueUpdate(
        String newNickname,
        String newPhoneNumber,
        String newEmail,
        User user
    ) {
        requireNicknameUniqueUpdate(newNickname, user);
        requireUniquePhoneNumberUpdate(newPhoneNumber, user);
        requireUniqueEmailUpdate(newEmail, user);
    }
}
