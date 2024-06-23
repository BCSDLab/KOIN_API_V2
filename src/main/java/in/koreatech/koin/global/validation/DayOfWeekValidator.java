package in.koreatech.koin.global.validation;

import java.util.List;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class DayOfWeekValidator implements ConstraintValidator<ValidDayOfWeek, String> {

    @Override
    public void initialize(ValidDayOfWeek constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(String inputDayOfWeek, ConstraintValidatorContext context) {
        List<String> dayOfWeeks = List.of(
            "MONDAY",
            "TUESDAY",
            "WEDNESDAY",
            "THURSDAY",
            "FRIDAY",
            "SATURDAY",
            "SUNDAY"
        );
        for (String dayOfWeek: dayOfWeeks) {
            if (dayOfWeek.equals(inputDayOfWeek)) {
                return true;
            }
        }
        return false;
    }
}
