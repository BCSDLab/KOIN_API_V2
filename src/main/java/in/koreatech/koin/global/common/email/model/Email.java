package in.koreatech.koin.global.common.email.model;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import in.koreatech.koin.global.common.email.exception.InvalidEmailFormatException;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Email {

    private static final EmailValidator emailValidator = new EmailValidator();

    static final String domainSeparator = "@";

    private final LocalParts localParts;
    private final Domain domain;

    public static Email from(String fullAddress) {
        emailValidator.validate(fullAddress);
        return new Email(LocalParts.from(fullAddress), Domain.from(fullAddress));
    }

    static int getSeparateIndex(String fullAddress) {
        return fullAddress.lastIndexOf(domainSeparator);
    }

    public void validateSendable() {
        domain.validate();
    }

    static class EmailValidator {
        public static final String LOCAL_PARTS_PATTERN = "^(?=.{1,64}@)[A-Za-z0-9\\+_-]+(\\.[A-Za-z0-9\\+_-]+)*@";
        public static final String DOMAIN_PATTERN = "[^-][A-Za-z0-9\\+-]+(\\.[A-Za-z0-9\\+-]+)*(\\.[A-Za-z]{2,})$";

        private static final String EMAIL_PATTERN = LOCAL_PARTS_PATTERN + DOMAIN_PATTERN;

        private final Pattern pattern = Pattern.compile(EMAIL_PATTERN);

        public final void validate(final String email) {
            Matcher matcher = pattern.matcher(email);
            if (!matcher.matches()) {
                throw InvalidEmailFormatException.withDetail("email: " + email);
            }
        }
    }
}
