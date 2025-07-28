package in.koreatech.koin.global.apiloadbalancer;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import io.swagger.v3.oas.annotations.Hidden;

@Hidden
@Retention(RUNTIME)
@Target(ElementType.TYPE)
public @interface ApiLoadBalance {

    int ratio();
}
