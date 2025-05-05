package in.koreatech.koin.infrastructure.email.model;

import in.koreatech.koin.infrastructure.email.exception.EmailAddressInvalidException;
import jakarta.validation.constraints.Email;

public record EmailAddress(
    @Email(message = "이메일 형식을 지켜주세요.", regexp = EmailAddress.EMAIL_PATTERN)
    String email
) {
    private static final String LOCAL_PARTS_PATTERN = "^(?=.{1,64}@)[A-Za-z0-9\\+_-]+(\\.[A-Za-z0-9\\+_-]+)*@";
    private static final String DOMAIN_PATTERN = "[^-][A-Za-z0-9\\+-]+(\\.[A-Za-z0-9\\+-]+)*(\\.[A-Za-z]{2,})$";
    private static final String EMAIL_PATTERN = LOCAL_PARTS_PATTERN + DOMAIN_PATTERN;
    private static final String ADMIN_EMAIL_PATTERN = "^koin\\d{5}$";

    private static final String DOMAIN_SEPARATOR = "@";
    private static final String KOREATECH_DOMAIN = "koreatech.ac.kr";

    public static EmailAddress from(String email) {
        return new EmailAddress(email);
    }

    public void validateKoreatechEmail() {
        if (!domainForm().equals(KOREATECH_DOMAIN)) {
            throw EmailAddressInvalidException.withDetail("account: " + email);
        }
    }

    public void validateAdminEmail() {
        if (!addressForm().matches(ADMIN_EMAIL_PATTERN)) {
            throw new EmailAddressInvalidException("어드민 계정 양식에 맞지 않습니다", "account: " + email);
        }
    }

    private String domainForm() {
        return email.substring(getSeparateIndex() + DOMAIN_SEPARATOR.length());
    }

    private int getSeparateIndex() {
        return email.lastIndexOf(DOMAIN_SEPARATOR);
    }

    private String addressForm() {
        return email.substring(0, email.lastIndexOf("@"));
    }
}
