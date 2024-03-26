package in.koreatech.koin.domain.owner.repository;

import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.owner.model.OwnerInVerification;
import in.koreatech.koin.global.domain.email.exception.EmailNotFoundException;

public interface OwnerInVerificationRedisRepository extends Repository<OwnerInVerification, String> {

    OwnerInVerification save(OwnerInVerification ownerInVerification);

    Optional<OwnerInVerification> findById(String email);

    void deleteById(String email);

    default OwnerInVerification getByEmail(String email) {
        return findById(email).orElseThrow(() -> EmailNotFoundException.withDetail("email: " + email));
    }
}
