package in.koreatech.koin.domain.student.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;

// SnakeCase X
public record StudentLoginResponse(
    @Schema(
        description = "Jwt accessToken",
        example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c",
        requiredMode = REQUIRED
    )
    @JsonProperty("token")
    String accessToken,

    @Schema(description = "Random UUID refresh token", example = "RANDOM-KEY-VALUE", requiredMode = REQUIRED)
    @JsonProperty("refresh_token")
    String refreshToken
) {

    public static StudentLoginResponse of(String token, String refreshToken) {
        return new StudentLoginResponse(token, refreshToken);
    }
}
