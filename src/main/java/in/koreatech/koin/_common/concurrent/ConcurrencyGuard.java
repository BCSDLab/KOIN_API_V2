package in.koreatech.koin._common.concurrent;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

import org.springframework.context.annotation.Profile;

@Documented
@Target(METHOD)
@Retention(RUNTIME)
@Profile("!test")
public @interface ConcurrencyGuard {

    String lockName();

    long waitTime() default 7L;

    long leaseTime() default 5L;

    TimeUnit timeUnit() default TimeUnit.SECONDS;
}
