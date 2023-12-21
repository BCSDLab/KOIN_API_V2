package in.koreatech.koin.domain.user.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class UserLoginResponse {

    @JsonProperty("token")
    private String accessToken;

    @JsonProperty("refresh_token")
    private String refreshToken;

    @JsonProperty("user_type")
    private String userType;

    public static UserLoginResponse of(String token, String refreshToken, String userType) {
        return new UserLoginResponse(token, refreshToken, userType);
    }
}
