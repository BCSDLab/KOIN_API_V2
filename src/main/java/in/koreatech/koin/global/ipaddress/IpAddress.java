package in.koreatech.koin.global.ipaddress;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import io.swagger.v3.oas.annotations.Hidden;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Hidden // Swagger 문서에 표시하지 않음
@Target(PARAMETER)
@Retention(RUNTIME)
public @interface IpAddress {

}
