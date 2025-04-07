package in.koreatech.koin.domain.user.repository;

import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.user.model.SmsDailyVerificationCount;

public interface SmsDailyVerificationCountRedisRepository extends Repository<SmsDailyVerificationCount, String> {

    SmsDailyVerificationCount save(SmsDailyVerificationCount smsDailyVerificationCount);

    Optional<SmsDailyVerificationCount> findByPhoneNumber(String phoneNumber);
}
