package in.koreatech.koin.domain.notification.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@JsonNaming(value = PropertyNamingStrategies.SnakeCaseStrategy.class)
public record NotificationPermitRequest(
    @Schema(description = "FCM 디바이스 토큰")
    @NotBlank String deviceToken
) {

}
