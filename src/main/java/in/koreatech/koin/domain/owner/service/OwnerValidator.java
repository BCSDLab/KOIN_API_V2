package in.koreatech.koin.domain.owner.service;

import static in.koreatech.koin.domain.user.model.UserType.OWNER;

import in.koreatech.koin.domain.owner.exception.DuplicationCompanyNumberException;
import in.koreatech.koin.domain.owner.exception.DuplicationPhoneNumberException;
import in.koreatech.koin.domain.owner.repository.OwnerRepository;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.repository.UserRepository;
import in.koreatech.koin.global.auth.exception.AuthorizationException;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OwnerValidator {

    private final UserRepository userRepository;
    private final OwnerRepository ownerRepository;

    public void validateAuth(User user) {
        if (!user.isAuthed()) {
            throw new AuthorizationException("미인증 상태입니다. 인증을 진행해주세요.");
        }
    }

    public void validateExistPhoneNumber(String phoneNumber) {
        if (userRepository.findByPhoneNumberAndUserType(phoneNumber, OWNER).isPresent()) {
            throw DuplicationPhoneNumberException.withDetail("account: " + phoneNumber);
        }
    }

    public void validateExistCompanyNumber(String companyNumber) {
        if (ownerRepository.findByCompanyRegistrationNumber(companyNumber).isPresent()) {
            throw DuplicationCompanyNumberException.withDetail("companyNumber: " + companyNumber);
        }
    }
}
