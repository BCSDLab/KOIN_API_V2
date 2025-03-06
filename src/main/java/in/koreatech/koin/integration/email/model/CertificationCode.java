package in.koreatech.koin.integration.email.model;

import lombok.Getter;

@Getter
public class CertificationCode {

    private final String value;

    private CertificationCode(String value) {
        this.value = value;
    }

    public static CertificationCode from(String value) {
        return new CertificationCode(value);
    }
}
