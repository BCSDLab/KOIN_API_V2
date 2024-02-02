package in.koreatech.koin.domain.owner.service;

import static in.koreatech.koin.global.common.email.model.MailForm.OWNER_REGISTRATION_MAIL_FORM;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.owner.dto.VerifyEmailRequest;
import in.koreatech.koin.domain.user.repository.UserRepository;
import in.koreatech.koin.global.common.email.exception.DuplicationEmailException;
import in.koreatech.koin.global.common.email.model.CertificationCode;
import in.koreatech.koin.global.common.email.model.Email;
import in.koreatech.koin.global.common.email.service.MailService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OwnerService {

    private final UserRepository userRepository;
    private final MailService mailService;

    public void requestVerificationToRegister(VerifyEmailRequest request) {
        Email email = Email.from(request.email());
        validateEmailUniqueness(email);

        CertificationCode certificationCode = mailService.sendMail(email, OWNER_REGISTRATION_MAIL_FORM);
    }

    private void validateEmailUniqueness(Email email) {
        userRepository.findByEmail(email.getEmail())
            .orElseThrow(() -> DuplicationEmailException.withDetail("이미 존재하는 이메일입니다. email: " + email));
    }
}
