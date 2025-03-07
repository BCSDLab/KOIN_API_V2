package in.koreatech.koin._common.util.random;

import static lombok.AccessLevel.PRIVATE;

import lombok.NoArgsConstructor;

@NoArgsConstructor(access = PRIVATE)
public class CertificateNumberGenerator {

    private static final int CERTIFICATION_NUMBER_ORIGIN = 0;
    private static final int CERTIFICATION_NUMBER_BOUND = 1_000_000;

    public static String generate() {
        return String.format("%06d",
            RandomGenerator.createNumber(CERTIFICATION_NUMBER_ORIGIN, CERTIFICATION_NUMBER_BOUND));
    }
}
