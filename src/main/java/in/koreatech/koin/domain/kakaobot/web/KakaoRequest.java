package in.koreatech.koin.domain.kakaobot.web;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import in.koreatech.koin.domain.kakaobot.model.KakaoRequestType;
import io.swagger.v3.oas.annotations.Hidden;

@Hidden // Swagger 문서에 표시하지 않음
@Target(PARAMETER)
@Retention(RUNTIME)
public @interface KakaoRequest {

    KakaoRequestType type();
}
