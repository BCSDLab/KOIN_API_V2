package in.koreatech.koin.global.domain.callcontol;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import io.swagger.v3.oas.annotations.Hidden;

@Hidden
@Retention(RUNTIME)
@Target(ElementType.METHOD)
public @interface CallControlInfo {

    int ratio();
}
