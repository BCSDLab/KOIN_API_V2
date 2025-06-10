package in.koreatech.koin.unit.domain.user.mock;

import in.koreatech.koin._common.util.random.VerificationNumberGenerator;

public class FakeVerificationNumberGenerator implements VerificationNumberGenerator {

    private final String fakeNumber;

    public FakeVerificationNumberGenerator(String fakeNumber) {
        this.fakeNumber = fakeNumber;
    }

    @Override
    public String generate() {
        return this.fakeNumber;
    }
}
