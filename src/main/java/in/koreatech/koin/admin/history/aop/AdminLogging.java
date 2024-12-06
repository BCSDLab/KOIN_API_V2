package in.koreatech.koin.admin.history.aop;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Profile;

import in.koreatech.koin.admin.history.enums.DomainType;

@Retention(RUNTIME)
@Target(METHOD)
@Profile("!test")
public @interface AdminLogging {
    DomainType domainType();

    boolean hasId() default false;
}
