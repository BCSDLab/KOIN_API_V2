package in.koreatech.koin.domain.user.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record UserLoginResponse(
    @JsonProperty("token") String accessToken,
    @JsonProperty("refresh_token") String refreshToken,
    @JsonProperty("user_type") String userType) {

    public static UserLoginResponse of(String token, String refreshToken, String userType) {
        return new UserLoginResponse(token, refreshToken, userType);
    }
}
