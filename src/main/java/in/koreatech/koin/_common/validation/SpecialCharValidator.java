package in.koreatech.koin._common.validation;

import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

@Component
public class SpecialCharValidator implements ConstraintValidator<SpecialCharNotAllowed, String> {

    private static final Pattern SPECIAL_CHAR_PATTERN = Pattern.compile("[/\\\\]");

    @Override
    public boolean isValid(String field, ConstraintValidatorContext context) {
        if (field == null) {
            return true;
        }

        return !SPECIAL_CHAR_PATTERN.matcher(field).find();
    }
}
