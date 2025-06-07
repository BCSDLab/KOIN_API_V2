package in.koreatech.koin.domain.user.dto.validation;

import com.fasterxml.jackson.databind.annotation.JsonNaming;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.*;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@JsonNaming(value = SnakeCaseStrategy.class)
public record UserAccessTokenRequest(
        @Schema(description = "access_token", example = "eyJ0eXAiOiJKV1QiLCJhbGcIUzUxMic9.eyJpZCI6NTM5NhwIjoxkzI3MTA5ODE5fQ.rLEYGQfKI5_24ZlwLVwlgwnriqySPKwXNOeTRrbmxoCtlOzCVvM8FFcO9BA2vkqsmhf-w", requiredMode = REQUIRED)
        @NotBlank(message = "access_token을 입력해주세요.")
        String accessToken
) {

}
