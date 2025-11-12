package in.koreatech.koin.domain.user.verification.repository;

import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.user.verification.model.VerificationCount;

public interface VerificationCountRedisRepository extends Repository<VerificationCount, String> {

    VerificationCount save(VerificationCount verificationCount);

    Optional<VerificationCount> findById(String id);

    void deleteById(String id);
}
