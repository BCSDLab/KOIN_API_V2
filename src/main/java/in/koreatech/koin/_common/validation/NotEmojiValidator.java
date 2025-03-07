package in.koreatech.koin._common.validation;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

@Component
public class NotEmojiValidator implements ConstraintValidator<NotEmoji, String> {

    private static final Pattern EMOJI_PATTERN = Pattern.compile("[\\uD83C-\\uDBFF\\uDC00-\\uDFFF]+");

    @Override
    public void initialize(NotEmoji constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(String field, ConstraintValidatorContext constraintValidatorContext) {
        if (field == null) {
            return true;
        }
        Matcher emojiMatcher = EMOJI_PATTERN.matcher(field);
        if (emojiMatcher.find()) {
            return false;
        }
        return true;
    }
}
