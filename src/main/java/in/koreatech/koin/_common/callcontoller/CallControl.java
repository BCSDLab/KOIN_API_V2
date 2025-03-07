package in.koreatech.koin._common.callcontoller;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import io.swagger.v3.oas.annotations.Hidden;

@Hidden
@Retention(RUNTIME)
@Target(ElementType.TYPE)
public @interface CallControl { //todo : 네이밍 변경 ex) CallControlInfo -> ApiCallBalancer or ApiLoacBalancer

    int ratio();
}
