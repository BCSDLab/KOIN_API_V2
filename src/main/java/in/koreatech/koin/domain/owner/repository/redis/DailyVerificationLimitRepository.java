package in.koreatech.koin.domain.owner.repository.redis;

import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.owner.model.redis.DailyVerificationLimit;

public interface DailyVerificationLimitRepository extends Repository<DailyVerificationLimit, String> {

    DailyVerificationLimit save(DailyVerificationLimit dailyVerificationLimit);

    Optional<DailyVerificationLimit> findById(String key);
}
