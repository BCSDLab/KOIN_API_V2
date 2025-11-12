package in.koreatech.koin.domain.user.verification.repository;

import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.user.verification.model.VerificationCode;

public interface VerificationCodeRedisRepository extends Repository<VerificationCode, String> {

    VerificationCode save(VerificationCode verificationCode);

    Optional<VerificationCode> findById(String id);

    void deleteById(String id);
}
