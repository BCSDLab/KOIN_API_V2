package in.koreatech.koin.global.validation;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Documented
@Constraint(validatedBy = NoSpecialCharactersValidator.class)
@Target({FIELD, PARAMETER, LOCAL_VARIABLE})
@Retention(RUNTIME)
public @interface NoSpecialCharacters {
    String message() default "입력값에 허용되지 않은 특수문자가 포함되어 있습니다.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
