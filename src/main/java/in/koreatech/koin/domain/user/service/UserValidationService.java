package in.koreatech.koin.domain.user.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.user.dto.EmailCheckExistsRequest;
import in.koreatech.koin.domain.user.dto.NicknameCheckExistsRequest;
import in.koreatech.koin.domain.user.dto.UserPasswordCheckRequest;
import in.koreatech.koin.domain.user.exception.DuplicationNicknameException;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.repository.UserRepository;
import in.koreatech.koin.global.domain.email.exception.DuplicationEmailException;
import in.koreatech.koin.global.exception.KoinIllegalArgumentException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserValidationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

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

    public void checkUserNickname(NicknameCheckExistsRequest request) {
        userRepository.findByNickname(request.nickname()).ifPresent(user -> {
            throw DuplicationNicknameException.withDetail("nickname: " + request.nickname());
        });
    }
}
