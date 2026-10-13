package in.koreatech.koin.global.auth;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import io.swagger.v3.oas.annotations.Hidden;

/**
 * 토큰으로부터 사용자 ID를 추출하여 가져온다.
 * <p>
 * Nullable: 인증정보가 없거나 유효하지 않으면 사용자 ID를 반환하지 않는다.
 */
@Hidden // Swagger 문서에 표시하지 않음
@Target(PARAMETER)
@Retention(RUNTIME)
public @interface UserId {

}
