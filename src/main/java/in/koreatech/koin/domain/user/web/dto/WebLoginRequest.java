package in.koreatech.koin.domain.user.web.dto;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.user.dto.UserLoginRequestV2;
import in.koreatech.koin.global.validation.NotEmoji;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@JsonNaming(value = SnakeCaseStrategy.class)
public record WebLoginRequest(
    @Schema(description = "아이디 또는 전화번호", example = "example1", requiredMode = REQUIRED)
    @NotBlank(message = "아이디 또는 전화번호를 입력해주세요.")
    @NotEmoji
    String loginId,

    @Schema(description = "비밀번호 (SHA 256 해싱된 값)", requiredMode = REQUIRED)
    @NotBlank(message = "비밀번호를 입력해주세요.")
    String loginPw,

    @Schema(description = "자동 로그인 여부. false이면 브라우저 세션 쿠키를 사용합니다.", defaultValue = "false")
    boolean autoLogin
) {

    public UserLoginRequestV2 toLoginRequest() {
        return new UserLoginRequestV2(loginId, loginPw);
    }

    @Override
    public String toString() {
        return "WebLoginRequest[REDACTED]";
    }
}
