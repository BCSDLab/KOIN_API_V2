package in.koreatech.koin.domain.user.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record UserTokenRefreshResponse(
    @JsonProperty("token") String accessToken,
    @JsonProperty("refresh_token") String refreshToken
) {

    public static UserTokenRefreshResponse of(String accessToken, String refreshToken) {
        return new UserTokenRefreshResponse(accessToken, refreshToken);
    }
}
