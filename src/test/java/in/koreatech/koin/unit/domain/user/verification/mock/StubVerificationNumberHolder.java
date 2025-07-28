package in.koreatech.koin.unit.domain.user.verification.mock;

import in.koreatech.koin.common.util.random.VerificationNumberGenerator;

public class StubVerificationNumberHolder implements VerificationNumberGenerator {

    private final String testNumber;

    public StubVerificationNumberHolder(String testNumber) {
        this.testNumber = testNumber;
    }

    @Override
    public String generate() {
        return this.testNumber;
    }
}
