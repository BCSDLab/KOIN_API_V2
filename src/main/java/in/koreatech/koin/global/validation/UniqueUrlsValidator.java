package in.koreatech.koin.global.validation;

import java.util.List;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class UniqueUrlsValidator implements ConstraintValidator<UniqueUrl, List<String>> {

    @Override
    public void initialize(UniqueUrl constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(List<String> elements, ConstraintValidatorContext context) {
        return elements.stream().distinct().count() == elements.size();
    }
}
