package in.koreatech.koin.global.concurrent;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

@Documented
@Target(METHOD)
@Retention(RUNTIME)
public @interface ConcurrencyGuard {

    String lockName();

    long waitTime() default 5L;

    long leaseTime() default 3L;

    TimeUnit timeUnit() default TimeUnit.SECONDS;
}
