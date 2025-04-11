package in.koreatech.koin.domain.user.repository;

import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.user.model.UserDailyVerifyCount;

public interface UserVerificationLimitRedisRepository extends Repository<UserDailyVerifyCount, String> {

    UserDailyVerifyCount save(UserDailyVerifyCount userdailyVerifyCount);

    Optional<UserDailyVerifyCount> findById(String id);
}
