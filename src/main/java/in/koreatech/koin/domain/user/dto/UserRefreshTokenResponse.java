package in.koreatech.koin.domain.user.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;

// SnakeCase X
public record UserRefreshTokenResponse(
    @Schema(description = "Jwt accessToken", example = "eyJhbGciOiJIUzI1NiIs...", requiredMode = REQUIRED)
    @JsonProperty("token")
    String accessToken,

    @Schema(description = "Random UUID refreshToken", example = "RANDOM-KEY-VALUE", requiredMode = REQUIRED)
    @JsonProperty("refresh_token")
    String refreshToken
) {

    public static UserRefreshTokenResponse of(String accessToken, String refreshToken) {
        return new UserRefreshTokenResponse(accessToken, refreshToken);
    }
}
