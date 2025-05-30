package in.koreatech.koin.domain.user.verification.repository;

import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.user.verification.model.UserVerificationStatus;
import in.koreatech.koin.infrastructure.email.exception.VerifyNotFoundException;

public interface UserVerificationStatusRedisRepository extends Repository<UserVerificationStatus, String> {

    UserVerificationStatus save(UserVerificationStatus userInVerification);

    Optional<UserVerificationStatus> findById(String id);

    void deleteById(String id);

    default UserVerificationStatus getById(String id) {
        return findById(id).orElseThrow(() -> VerifyNotFoundException.withDetail("verification: " + id));
    }
}
