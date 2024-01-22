package in.koreatech.koin.domain.owner.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.owner.dto.VerifyEmailRequest;
import in.koreatech.koin.domain.user.repository.UserRepository;
import in.koreatech.koin.global.common.email.exception.DuplicationEmailException;
import in.koreatech.koin.global.common.email.model.Email;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OwnerService {

    private final UserRepository userRepository;

    public void requestVerificationToRegister(VerifyEmailRequest request) {
        Email email = Email.from(request.email());
        validateEmailUniqueness(email);
        
    }

    private void validateEmailUniqueness(Email email) {
        userRepository.findByEmail(email.getEmail())
            .orElseThrow(() -> DuplicationEmailException.withDetail("이미 존재하는 이메일입니다. email: " + email));
    }
}
