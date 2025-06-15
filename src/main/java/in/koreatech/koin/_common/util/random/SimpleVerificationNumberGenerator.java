package in.koreatech.koin._common.util.random;

import org.springframework.stereotype.Component;

@Component
public class SimpleVerificationNumberGenerator implements VerificationNumberGenerator {

    private static final int CERTIFICATION_NUMBER_ORIGIN = 0;
    private static final int CERTIFICATION_NUMBER_BOUND = 1_000_000;

    @Override
    public String generate() {
        return String.format("%06d",
            RandomGenerator.createNumber(CERTIFICATION_NUMBER_ORIGIN, CERTIFICATION_NUMBER_BOUND));
    }
}
