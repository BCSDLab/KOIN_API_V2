package in.koreatech.koin.global.validation;

import java.util.List;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class UniqueIdValidator implements ConstraintValidator<UniqueId, List<Integer>> {

    @Override
    public void initialize(UniqueId constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(List<Integer> elements, ConstraintValidatorContext context) {
        return elements.stream().distinct().count() == elements.size();
    }
}
