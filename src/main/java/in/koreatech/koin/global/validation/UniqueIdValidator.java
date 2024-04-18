package in.koreatech.koin.global.validation;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class UniqueIdValidator implements ConstraintValidator<UniqueId, List<Integer>> {

    @Override
    public void initialize(UniqueId constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(List<Integer> elements, ConstraintValidatorContext context) {
        Set<Integer> set = new HashSet<>(elements.size());
        for (Integer element : elements) {
            if (!set.add(element)) {
                return false;
            }
        }
        return true;
    }
}
