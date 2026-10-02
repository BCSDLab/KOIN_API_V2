package in.koreatech.koin.domain.user.web.dto;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.fasterxml.jackson.databind.annotation.JsonNaming;

import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(value = SnakeCaseStrategy.class)
public record WebSessionResponse(
    @Schema(description = "웹 로그인 세션이 유효한지 여부. 비로그인은 오류가 아니라 false로 응답합니다.", example = "true", requiredMode = REQUIRED)
    boolean authenticated,

    @Schema(description = "회원 유형. authenticated가 true일 때만 존재합니다.", example = "STUDENT", requiredMode = NOT_REQUIRED, nullable = true)
    String userType,

    @Schema(description = "CSRF 일반 쿠키와 동일한 세션별 HMAC 토큰. authenticated가 true일 때만 존재합니다.", requiredMode = NOT_REQUIRED, nullable = true)
    String csrfToken
) {

    public static WebSessionResponse anonymous() {
        return new WebSessionResponse(false, null, null);
    }
}
