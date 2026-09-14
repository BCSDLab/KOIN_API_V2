package in.koreatech.koin.global.validation;

import org.springframework.stereotype.Component;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

@Component
public class NotEmojiValidator implements ConstraintValidator<NotEmoji, String> {

    private static final int EMOJI_VARIATION_SELECTOR = 0xFE0F;

    @Override
    public void initialize(NotEmoji constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(String field, ConstraintValidatorContext constraintValidatorContext) {
        if (field == null) {
            return true;
        }
        return field.codePoints().noneMatch(codePoint ->
            Character.isSupplementaryCodePoint(codePoint)
                || codePoint == EMOJI_VARIATION_SELECTOR
                || isIsolatedSurrogate(codePoint)
        );
    }

    private static boolean isIsolatedSurrogate(int codePoint) {
        return codePoint >= Character.MIN_SURROGATE && codePoint <= Character.MAX_SURROGATE;
    }
}
