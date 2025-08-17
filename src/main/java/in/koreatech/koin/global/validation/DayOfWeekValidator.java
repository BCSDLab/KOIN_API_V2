package in.koreatech.koin.global.validation;

import java.util.Set;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class DayOfWeekValidator implements ConstraintValidator<DayOfWeek, String> {
    private final Set<String> validDays = Set.of(
        "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY"
    );

    @Override
    public void initialize(DayOfWeek constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(String dayOfWeek, ConstraintValidatorContext context) {
        if (dayOfWeek == null) {
            return false;
        }
        return validDays.contains(dayOfWeek.toUpperCase());
    }
}
