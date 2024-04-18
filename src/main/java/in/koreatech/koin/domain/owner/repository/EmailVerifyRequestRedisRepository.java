package in.koreatech.koin.domain.owner.repository;

import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.owner.model.EmailVerifyRequest;

public interface EmailVerifyRequestRedisRepository extends Repository<EmailVerifyRequest, String> {

    EmailVerifyRequest save(EmailVerifyRequest emailVerifyRequest);

    Optional<EmailVerifyRequest> findById(String email);
}
