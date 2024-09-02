package in.koreatech.koin.global.validation;

import java.util.List;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class NotBlankElementValidator implements ConstraintValidator<NotBlankElement, List<String>> {

    @Override
    public void initialize(NotBlankElement constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(List<String> elements, ConstraintValidatorContext context) {
        return elements != null && elements.stream().allMatch(it -> it != null && !it.isBlank());
    }
}
