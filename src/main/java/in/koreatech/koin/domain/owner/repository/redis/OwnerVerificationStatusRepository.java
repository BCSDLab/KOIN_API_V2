package in.koreatech.koin.domain.owner.repository.redis;

import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.owner.model.redis.OwnerVerificationStatus;
import in.koreatech.koin.global.marker.RedisRepositoryMarker;
import in.koreatech.koin.infrastructure.email.exception.VerifyNotFoundException;

@RedisRepositoryMarker
public interface OwnerVerificationStatusRepository extends Repository<OwnerVerificationStatus, String> {

    OwnerVerificationStatus save(OwnerVerificationStatus ownerInVerification);

    Optional<OwnerVerificationStatus> findById(String key);

    void deleteById(String key);

    default void deleteByVerify(String key) {
        deleteById(key);
    }

    default OwnerVerificationStatus getByVerify(String key) {
        return findById(key).orElseThrow(() -> VerifyNotFoundException.withDetail("verify: " + key));
    }
}
