package in.koreatech.koin.global.validation;

import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

@Component
public class EmojiValidator implements ConstraintValidator<NotEmoji, String> {

    private static final Pattern EMOJI_PATTERN = Pattern.compile(
        "[\\uD83C-\\uDBFF\\uDC00-\\uDFFF]|" +     // 서플리먼터리
            "[\\u2600-\\u26FF]|" +                // Miscellaneous Symbols
            "[\\u2700-\\u27BF]|" +                // Dingbats
            "[\\u1F300-\\u1F5FF]|" +              // Miscellaneous Symbols and Pictographs
            "[\\u1F600-\\u1F64F]|" +              // Emoticons
            "[\\u1F680-\\u1F6FF]|" +              // Transport and Map Symbols
            "[\\u1F700-\\u1F77F]|" +              // Alchemical Symbols
            "[\\u1F780-\\u1F7FF]|" +              // Geometric Shapes Extended
            "[\\u1F800-\\u1F8FF]|" +              // Supplemental Arrows-C
            "[\\u1F900-\\u1F9FF]|" +              // Supplemental Symbols and Pictographs
            "[\\u1FA00-\\u1FA6F]|" +              // Chess Symbols
            "[\\u1FA70-\\u1FAFF]"                 // Symbols and Pictographs Extended-A
    );

    @Override
    public void initialize(NotEmoji constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(String field, ConstraintValidatorContext constraintValidatorContext) {
        for (int i = 0; i < field.length(); i++) {
            int codePoint = field.codePointAt(i);
            if (Character.isSupplementaryCodePoint(codePoint)) {
                i++;
            }
            if (isEmoji(codePoint)) {
                return false;
            }
        }
        return true;
    }

    private boolean isEmoji(int codePoint) {
        return EMOJI_PATTERN.matcher(new String(Character.toChars(codePoint))).find();
    }
}
