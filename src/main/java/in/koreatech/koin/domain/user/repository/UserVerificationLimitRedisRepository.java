package in.koreatech.koin.domain.user.repository;

import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.user.model.UserDailyVerificationLimit;

public interface UserVerificationLimitRedisRepository extends Repository<UserDailyVerificationLimit, String> {

    UserDailyVerificationLimit save(UserDailyVerificationLimit userdailyVerificationLimit);

    Optional<UserDailyVerificationLimit> findById(String key);
}
