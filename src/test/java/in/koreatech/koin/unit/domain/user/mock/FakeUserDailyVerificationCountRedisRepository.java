package in.koreatech.koin.unit.domain.user.mock;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import in.koreatech.koin.domain.user.verification.model.UserDailyVerificationCount;
import in.koreatech.koin.domain.user.verification.repository.UserDailyVerificationCountRedisRepository;

public class FakeUserDailyVerificationCountRedisRepository implements UserDailyVerificationCountRedisRepository {

    private final Map<String, UserDailyVerificationCount> store = new HashMap<>();

    @Override
    public UserDailyVerificationCount save(UserDailyVerificationCount userDailyVerificationCount) {
        store.put(userDailyVerificationCount.getId(), userDailyVerificationCount);
        return userDailyVerificationCount;
    }

    @Override
    public Optional<UserDailyVerificationCount> findById(String id) {
        return Optional.ofNullable(store.get(id));
    }
}
