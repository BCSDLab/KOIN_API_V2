package in.koreatech.koin.domain.user.repository;

import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.user.model.UserVerificationStatus;
import in.koreatech.koin.integration.email.exception.VerifyNotFoundException;

public interface UserVerificationStatusRedisRepository extends Repository<UserVerificationStatus, Long> {

    UserVerificationStatus save(UserVerificationStatus userInVerification);

    Optional<UserVerificationStatus> findById(String key);

    void deleteById(String key);

    default void deleteByVerify(String key) {
        deleteById(key);
    }

    default UserVerificationStatus getByVerify(String key) {
        return findById(key).orElseThrow(() -> VerifyNotFoundException.withDetail("verify: " + key));
    }
}
