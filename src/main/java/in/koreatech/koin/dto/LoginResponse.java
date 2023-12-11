package in.koreatech.koin.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = lombok.AccessLevel.PRIVATE)
@JsonNaming(value = PropertyNamingStrategies.SnakeCaseStrategy.class)
public class LoginResponse {

    private String token;

    @JsonProperty("refresh_token")
    private String refreshToken;
    @JsonProperty("user_type")
    private String userType;
}
