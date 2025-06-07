package in.koreatech.koin._common.validation;

import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

@Component
public class SpecialCharValidator implements ConstraintValidator<SpecialCharNotAllowed, String> {

    // 슬래시(/) 또는 백슬래시(\) 중 하나라도 포함되었는지 검사하는 정규식
    private static final Pattern ILLEGAL_PATTERN = Pattern.compile("[/\\\\]");

    @Override
    public boolean isValid(String field, ConstraintValidatorContext context) {
        if (field == null) {
            return true;
        }

        return !ILLEGAL_PATTERN.matcher(field).find();
    }
}
