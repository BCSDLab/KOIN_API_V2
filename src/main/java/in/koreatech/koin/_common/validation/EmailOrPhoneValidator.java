package in.koreatech.koin._common.validation;

import java.util.regex.Pattern;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class EmailOrPhoneValidator implements ConstraintValidator<EmailOrPhone, String> {

    private static final Pattern EMAIL_PATTERN =
        Pattern.compile("^[a-zA-Z0-9._%+-]+@koreatech.ac.kr$");

    private static final Pattern PHONE_PATTERN =
        Pattern.compile("^\\d{11}$"); // 한국 휴대폰 번호 형식

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return false;
        }

        return EMAIL_PATTERN.matcher(value).matches()
            || PHONE_PATTERN.matcher(value).matches();
    }
}
