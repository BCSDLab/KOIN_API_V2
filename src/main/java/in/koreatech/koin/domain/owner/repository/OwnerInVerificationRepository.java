package in.koreatech.koin.domain.owner.repository;

import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.owner.model.OwnerInVerification;
import in.koreatech.koin.global.common.email.exception.EmailNotFoundException;

public interface OwnerInVerificationRepository extends Repository<OwnerInVerification, String> {

    OwnerInVerification save(OwnerInVerification ownerInVerification);

    Optional<OwnerInVerification> findByEmail(String email);

    default OwnerInVerification getByEmail(String email) {
        return findByEmail(email).orElseThrow(() -> EmailNotFoundException.withDetail("email: " + email));
    }
}
