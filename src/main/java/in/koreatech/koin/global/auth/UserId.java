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

    /**
     * 공개 조회 API에서 토큰 인증에 실패하면 비로그인 사용자로 처리한다.
     */
    boolean ignoreInvalidToken() default false;
}
