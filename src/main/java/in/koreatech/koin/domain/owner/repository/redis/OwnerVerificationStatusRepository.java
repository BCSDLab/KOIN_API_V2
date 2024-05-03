package in.koreatech.koin.domain.owner.repository.redis;

import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.owner.model.redis.OwnerVerificationStatus;
import in.koreatech.koin.global.domain.email.exception.VerifyNotFoundException;

public interface OwnerVerificationStatusRepository extends Repository<OwnerVerificationStatus, String> {

    OwnerVerificationStatus save(OwnerVerificationStatus ownerInVerification);

    Optional<OwnerVerificationStatus> findById(String email);

    void deleteById(String email);

    default void deleteByVerify(String email) {
        deleteById(email);
    }

    default OwnerVerificationStatus getByVerify(String verify) {
        return findById(verify).orElseThrow(() -> VerifyNotFoundException.withDetail("verify: " + verify));
    }
}
