package in.koreatech.koin.global.domain.notification.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record NotificationStatusResponse(
    @Schema(description = "푸쉬 알림 동의 여부")
    boolean isPermit
) {

}
