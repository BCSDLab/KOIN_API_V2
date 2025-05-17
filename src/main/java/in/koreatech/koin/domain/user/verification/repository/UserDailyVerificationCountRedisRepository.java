package in.koreatech.koin.domain.user.verification.repository;

import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.user.verification.model.UserDailyVerificationCount;

public interface UserDailyVerificationCountRedisRepository extends Repository<UserDailyVerificationCount, String> {

    UserDailyVerificationCount save(UserDailyVerificationCount userDailyVerificationCount);

    Optional<UserDailyVerificationCount> findById(String id);
}
