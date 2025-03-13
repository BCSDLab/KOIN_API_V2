package in.koreatech.koin._common.validation;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Documented
@Constraint(validatedBy = NotEmojiValidator.class)
@Target({FIELD, PARAMETER, LOCAL_VARIABLE})
@Retention(RUNTIME)
public @interface NotEmoji {
    String message() default "이모지가 허용되지 않습니다.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
