package in.koreatech.koin.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record NotificationPermitRequest(
    @Schema(description = "FCM 디바이스 토큰")
    @NotBlank String deviceToken
) {
}
