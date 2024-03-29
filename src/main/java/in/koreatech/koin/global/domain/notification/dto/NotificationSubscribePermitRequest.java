package in.koreatech.koin.global.domain.notification.dto;

import in.koreatech.koin.global.domain.notification.model.NotificationSubscribeType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record NotificationSubscribePermitRequest(
    @Schema(example = "SHOP_EVENT", description = "타입")
    @NotNull NotificationSubscribeType type
) {

}
