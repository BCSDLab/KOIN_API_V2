package in.koreatech.koin.domain.user.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@JsonNaming(value = SnakeCaseStrategy.class)
public record UserTokenRefreshRequest(
    @Schema(description = "refresh_token", example = "eyJhbGciOiJIUzI1NiJ9", requiredMode = REQUIRED)
    @NotNull(message = "refresh_token을 입력해주세요.")
    @JsonProperty("refresh_token")
    String refreshToken
) {

}
