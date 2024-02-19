package in.koreatech.koin.domain.user.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;

public record UserLoginResponse(
    @Schema(description = "Jwt accessToken", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c")
    @JsonProperty("token") String accessToken,

    @Schema(description = "Random UUID refresh token", example = "RANDOM-KEY-VALUE")
    @JsonProperty("refresh_token") String refreshToken,

    @Schema(
        description = """
            로그인한 회원의 신원
            - `STUDENT`: 학생
            - `OWNER`: 사장님"
                """, example = "STUDENT"
    )
    @JsonProperty("user_type") String userType
) {

    public static UserLoginResponse of(String token, String refreshToken, String userType) {
        return new UserLoginResponse(token, refreshToken, userType);
    }
}
