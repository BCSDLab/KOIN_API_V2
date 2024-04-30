package in.koreatech.koin.domain.owner.repository;

import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.owner.model.OwnerInVerification;
import in.koreatech.koin.global.domain.email.exception.VerifyNotFoundException;

public interface OwnerInVerificationRedisRepository extends Repository<OwnerInVerification, String> {

    OwnerInVerification save(OwnerInVerification ownerInVerification);

    Optional<OwnerInVerification> findById(String email);

    void deleteById(String email);

    default void deleteByVerify(String email) {
        deleteById(email);
    }

    default OwnerInVerification getByVerify(String verify) {
        return findById(verify).orElseThrow(() -> VerifyNotFoundException.withDetail("verify: " + verify));
    }
}
