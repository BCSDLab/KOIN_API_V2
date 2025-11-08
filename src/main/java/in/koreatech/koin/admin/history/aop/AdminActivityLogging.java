package in.koreatech.koin.admin.history.aop;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import in.koreatech.koin.admin.history.enums.DomainType;

@Target(METHOD)
@Retention(RUNTIME)
public @interface AdminActivityLogging {
    DomainType domain();
    String domainIdParam() default "";
}
