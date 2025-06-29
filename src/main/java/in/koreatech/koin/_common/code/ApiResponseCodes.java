package in.koreatech.koin._common.code;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ApiResponseCodes {
    ApiResponseCode[] value();
}
