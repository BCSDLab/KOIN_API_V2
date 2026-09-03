package in.koreatech.koin.global.code;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Documented
@Target(METHOD)
@Retention(RUNTIME)
public @interface Deprecation {

    String since() default "";

    String reason() default "";

    String replacedByMethod() default "";

    String replacedByPath() default "";

    boolean forRemoval() default false;
}
