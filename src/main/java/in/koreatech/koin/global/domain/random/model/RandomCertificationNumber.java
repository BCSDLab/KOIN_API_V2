package in.koreatech.koin.global.domain.random.model;

import in.koreatech.koin.global.domain.email.model.CertificationCode;

public class RandomCertificationNumber {

    private static final int CERTIFICATION_NUMBER_ORIGIN = 0;
    private static final int CERTIFICATION_NUMBER_BOUND = 1_000_000;

    public static CertificationCode getCertificationCode() {
        return CertificationCode.from(String.format("%06d",
            RandomGenerator.createNumber(CERTIFICATION_NUMBER_ORIGIN, CERTIFICATION_NUMBER_BOUND)));
    }
}
