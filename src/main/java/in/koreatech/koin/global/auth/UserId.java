package in.koreatech.koin.global.auth;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import io.swagger.v3.oas.annotations.Hidden;

/**
 * 토큰으로부터 사용자 ID를 추출하여 가져온다.
 * <p>
 * Nullable: 사용자 ID가 없을 수도 있다.
 */
@Hidden // Swagger 문서에 표시하지 않음
@Target(PARAMETER)
@Retention(RUNTIME)
public @interface UserId {

}
