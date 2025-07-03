package in.koreatech.koin.unit.domain.user.verification.mock;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import in.koreatech.koin.domain.user.verification.model.VerificationCode;
import in.koreatech.koin.domain.user.verification.repository.VerificationCodeRedisRepository;

public class FakeVerificationCodeRedisRepository implements VerificationCodeRedisRepository {

    private final Map<String, VerificationCode> store = new HashMap<>();

    @Override
    public VerificationCode save(VerificationCode verificationCode) {
        store.put(verificationCode.getId(), verificationCode);
        return verificationCode;
    }

    @Override
    public Optional<VerificationCode> findById(String id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public void deleteById(String id) {
        store.remove(id);
    }
}
