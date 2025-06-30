package in.koreatech.koin.unit.domain.user.verification.mock;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import in.koreatech.koin.domain.user.verification.model.VerificationCount;
import in.koreatech.koin.domain.user.verification.repository.VerificationCountRedisRepository;

public class FakeVerificationCountRedisRepository implements VerificationCountRedisRepository {

    private final Map<String, VerificationCount> store = new HashMap<>();

    @Override
    public VerificationCount save(VerificationCount verificationCount) {
        store.put(verificationCount.getId(), verificationCount);
        return verificationCount;
    }

    @Override
    public Optional<VerificationCount> findById(String id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public void deleteById(String id) {
        store.remove(id);
    }
}
