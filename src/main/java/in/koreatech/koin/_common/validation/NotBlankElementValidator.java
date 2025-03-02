package in.koreatech.koin._common.validation;

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
        if (elements == null) {
            elements = List.of();
        }
        return elements.stream().noneMatch(it -> it == null || it.isBlank());
    }
}
