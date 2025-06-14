package in.koreatech.koin.domain.user.verification.repository;

import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.user.verification.model.UserVerificationStatus;

public interface UserVerificationStatusRedisRepository extends Repository<UserVerificationStatus, String> {

    UserVerificationStatus save(UserVerificationStatus userInVerification);

    Optional<UserVerificationStatus> findById(String id);

    void deleteById(String id);
}
