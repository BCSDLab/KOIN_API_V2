package in.koreatech.koin.global.validation;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class UniqueUrlsValidator implements ConstraintValidator<UniqueUrl, List<String>> {

    @Override
    public void initialize(UniqueUrl constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(List<String> elements, ConstraintValidatorContext context) {
        Set<String> set = new HashSet<>(elements.size());
        for (String element : elements) {
            if (!set.add(element)) {
                return false;
            }
        }
        return true;
    }
}
