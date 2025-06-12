package in.koreatech.koin.unit.domain.user.verification.mock;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import in.koreatech.koin.domain.user.verification.model.UserVerificationStatus;
import in.koreatech.koin.domain.user.verification.repository.UserVerificationStatusRedisRepository;

public class FakeUserVerificationStatusRedisRepository implements UserVerificationStatusRedisRepository {

    private final Map<String, UserVerificationStatus> store = new HashMap<>();

    @Override
    public UserVerificationStatus save(UserVerificationStatus userVerificationStatus) {
        store.put(userVerificationStatus.getId(), userVerificationStatus);
        return userVerificationStatus;
    }

    @Override
    public Optional<UserVerificationStatus> findById(String id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public void deleteById(String id) {
        store.remove(id);
    }
}
