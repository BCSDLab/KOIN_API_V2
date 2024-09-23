package in.koreatech.koin.domain.user.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

public record UserAccessTokenRequest(
        @Schema(description = "access_token", example = "eyJ0eXAiOiJKV1QiLCJhbGcIUzUxMic9.eyJpZCI6NTM5NhwIjoxkzI3MTA5ODE5fQ.rLEYGQfKI5_24ZlwLVwlgwnriqySPKwXNOeTRrbmxoCtlOzCVvM8FFcO9BA2vkqsmhf-w", requiredMode = REQUIRED)
        @NotBlank(message = "access_token을 입력해주세요.")
        @NotNull(message = "access_token을 입력해주세요.")
        @JsonProperty("access_token")
        String accessToken
) {

}
