package in.koreatech.koin.domain.user.repository;

import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.user.model.UserVerificationStatus;
import in.koreatech.koin.integration.email.exception.VerifyNotFoundException;

public interface UserVerificationStatusRedisRepository extends Repository<UserVerificationStatus, String> {

    UserVerificationStatus save(UserVerificationStatus userInVerification);

    Optional<UserVerificationStatus> findById(String phoneNumber);

    void deleteById(String phoneNumber);

    default UserVerificationStatus getById(String phoneNumber) {
        return findById(phoneNumber).orElseThrow(
            () -> VerifyNotFoundException.withDetail("verify: " + phoneNumber));
    }
}
