package in.koreatech.koin.domain.user.verification.repository;

import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.user.verification.model.VerificationCount;
import in.koreatech.koin.config.repository.RedisRepository;

@RedisRepository
public interface VerificationCountRedisRepository extends Repository<VerificationCount, String> {

    VerificationCount save(VerificationCount verificationCount);

    Optional<VerificationCount> findById(String id);

    void deleteById(String id);
}
