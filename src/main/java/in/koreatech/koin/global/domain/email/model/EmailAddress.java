package in.koreatech.koin.global.domain.email.model;

import jakarta.validation.constraints.Email;

public record EmailAddress(
    @Email(message = "이메일 형식을 지켜주세요.", regexp = EmailAddress.EMAIL_PATTERN)
    String email
) {
    private static final String LOCAL_PARTS_PATTERN = "^(?=.{1,64}@)[A-Za-z0-9\\+_-]+(\\.[A-Za-z0-9\\+_-]+)*@";
    private static final String DOMAIN_PATTERN = "[^-][A-Za-z0-9\\+-]+(\\.[A-Za-z0-9\\+-]+)*(\\.[A-Za-z]{2,})$";
    private static final String EMAIL_PATTERN = LOCAL_PARTS_PATTERN + DOMAIN_PATTERN;
}
