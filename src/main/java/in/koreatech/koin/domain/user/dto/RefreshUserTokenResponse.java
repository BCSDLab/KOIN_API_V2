package in.koreatech.koin.domain.user.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;

// SnakeCase X
public record RefreshUserTokenResponse(
    @Schema(
        description = "Jwt accessToken",
        example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c",
        requiredMode = REQUIRED
    )
    @JsonProperty("token")
    String accessToken,

    @Schema(description = "Random UUID refreshToken", example = "RANDOM-KEY-VALUE", requiredMode = REQUIRED)
    @JsonProperty("refresh_token")
    String refreshToken
) {

    public static RefreshUserTokenResponse of(String accessToken, String refreshToken) {
        return new RefreshUserTokenResponse(accessToken, refreshToken);
    }
}
