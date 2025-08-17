package in.koreatech.koin.admin.operator.service;

import static in.koreatech.koin.domain.user.model.UserType.ADMIN;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import in.koreatech.koin.admin.operator.dto.request.AdminLoginRequest;
import in.koreatech.koin.admin.operator.repository.AdminRepository;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.global.auth.exception.AuthorizationException;
import in.koreatech.koin.global.code.ApiResponseCode;
import in.koreatech.koin.global.exception.CustomException;
import in.koreatech.koin.infrastructure.email.exception.DuplicationEmailException;
import in.koreatech.koin.infrastructure.email.model.EmailAddress;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AdminValidation {

    private final PasswordEncoder passwordEncoder;
    private final AdminRepository adminRepository;

    public void validateEmailForAdminCreated(String email) {
        EmailAddress emailAddress = EmailAddress.from(email);
        emailAddress.validateKoreatechEmail();
        emailAddress.validateAdminEmail();

        validateDuplicateEmail(email);
    }

    private void validateDuplicateEmail(String email) {
        adminRepository.findByEmail(email)
            .ifPresent(user -> {
                throw DuplicationEmailException.withDetail("account: " + email);
            });
    }

    public void validateAdminLogin(User user, AdminLoginRequest request) {
        /* 어드민 권한이 없으면 없는 회원으로 간주 */
        if (user.getUserType() != ADMIN) {
            throw CustomException.of(ApiResponseCode.NOT_FOUND_USER, "account" + request.email());
        }

        if (adminRepository.findById(user.getId()).isEmpty()) {
            throw CustomException.of(ApiResponseCode.NOT_FOUND_USER, "account" + request.email());
        }

        user.requireSameLoginPw(passwordEncoder, request.password());

        if (!user.isAuthed()) {
            throw new AuthorizationException("PL 인증 대기중입니다.");
        }
    }
}
