package in.koreatech.koin.admin.user.validation;

import static in.koreatech.koin.domain.user.model.UserType.ADMIN;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import in.koreatech.koin.admin.user.dto.AdminLoginRequest;
import in.koreatech.koin.admin.user.repository.AdminRepository;
import in.koreatech.koin.admin.user.repository.AdminUserRepository;
import in.koreatech.koin.domain.user.exception.UserNotFoundException;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin._common.auth.exception.AuthorizationException;
import in.koreatech.koin.infrastructure.email.exception.DuplicationEmailException;
import in.koreatech.koin.infrastructure.email.model.EmailAddress;
import in.koreatech.koin._common.exception.custom.KoinIllegalArgumentException;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AdminUserValidation {

    private final PasswordEncoder passwordEncoder;
    private final AdminRepository adminRepository;
    private final AdminUserRepository adminUserRepository;

    public void validateEmailForAdminCreated(String email) {
        EmailAddress emailAddress = EmailAddress.from(email);
        emailAddress.validateKoreatechEmail();
        emailAddress.validateAdminEmail();

        validateDuplicateEmail(email);
    }

    private void validateDuplicateEmail(String email) {
        adminUserRepository.findByEmail(email)
            .ifPresent(user -> {
                throw DuplicationEmailException.withDetail("account: " + email);
            });
    }

    public void validateAdminLogin(User user, AdminLoginRequest request) {
        /* 어드민 권한이 없으면 없는 회원으로 간주 */
        if (user.getUserType() != ADMIN) {
            throw UserNotFoundException.withDetail("account" + request.email());
        }

        if (adminRepository.findById(user.getId()).isEmpty()) {
            throw UserNotFoundException.withDetail("account" + request.email());
        }

        if (!user.isSamePassword(passwordEncoder, request.password())) {
            throw new KoinIllegalArgumentException("비밀번호가 틀렸습니다.");
        }

        if (!user.isAuthed()) {
            throw new AuthorizationException("PL 인증 대기중입니다.");
        }
    }
}
