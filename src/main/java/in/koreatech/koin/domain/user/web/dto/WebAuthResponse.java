package in.koreatech.koin.domain.user.web.dto;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.fasterxml.jackson.databind.annotation.JsonNaming;

import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(value = SnakeCaseStrategy.class)
public record WebAuthResponse(
    @Schema(description = "회원 유형", example = "STUDENT", requiredMode = REQUIRED)
    String userType,

    @Schema(description = "CSRF 일반 쿠키와 동일한 세션별 HMAC 토큰. 상태 변경 요청의 X-CSRF-Token 헤더에 전달합니다.", requiredMode = REQUIRED)
    String csrfToken
) {

}
