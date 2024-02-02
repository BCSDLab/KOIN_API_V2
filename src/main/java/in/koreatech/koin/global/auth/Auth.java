package in.koreatech.koin.global.auth;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import in.koreatech.koin.domain.user.model.UserType;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target(PARAMETER)
@Retention(RUNTIME)
public @interface Auth {

    UserType[] permit() default {};

    boolean anonymous() default false;
}
