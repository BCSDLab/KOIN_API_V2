package in.koreatech.koin.global.validation;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Constraint(validatedBy = FileTypeValidator.class)
@Target({PARAMETER})
@Retention(RUNTIME)
public @interface FileTypeValid {

    String message() default "허용되지 않는 파일 형식입니다.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    String[] extensions() default {};

    boolean nullable() default false;
}
