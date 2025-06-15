package in.koreatech.koin._common.validation;

import org.springframework.stereotype.Component;

import com.vdurmont.emoji.EmojiParser;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

@Component
public class NotEmojiValidator implements ConstraintValidator<NotEmoji, String> {

    @Override
    public void initialize(NotEmoji constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(String field, ConstraintValidatorContext constraintValidatorContext) {
        if (field == null) return true;
        return EmojiParser.extractEmojis(field).isEmpty();
    }
}
