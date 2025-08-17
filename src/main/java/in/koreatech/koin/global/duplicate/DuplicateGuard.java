package in.koreatech.koin.global.duplicate;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 동시 호출 (따닥) 방지
 * timeoutSeconds 동안 동일한 요청이 전달되는 경우, 자동으로 409 CONFLICT 예외를 반환합니다 (디폴트 0.1초)
 * key 값은 sEPL 표현식으로 작성할 수 있습니다.
 * */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DuplicateGuard {
    String key();
    long timeoutSeconds() default 100;
}
