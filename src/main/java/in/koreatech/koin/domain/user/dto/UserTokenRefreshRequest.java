package in.koreatech.koin.domain.user.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.NotNull;

@JsonNaming(value = SnakeCaseStrategy.class)
public record UserTokenRefreshRequest(
    @NotNull(message = "refresh_token을 입력해주세요.") String refreshToken
) {

}
