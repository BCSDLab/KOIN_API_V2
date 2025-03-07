package in.koreatech.koin._common.validation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Constraint(validatedBy = SingleMenuPriceValidator.class)
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface SingleMenuPrice {

    String message() default "단일 메뉴 유효성 검사에 실패했습니다.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
