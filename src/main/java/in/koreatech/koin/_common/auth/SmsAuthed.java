package in.koreatech.koin._common.auth;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import io.swagger.v3.oas.annotations.Hidden;

/**
 * SMS 인증 후 발급받은 임시 토큰에서 휴대폰 번호를 추출하여 주입받습니다.
 * 토큰이 없거나 유효하지 않은 경우 AuthenticationException이 발생합니다.
 */
@Hidden
@Target(PARAMETER)
@Retention(RUNTIME)
public @interface SmsAuthed {
    
} 
