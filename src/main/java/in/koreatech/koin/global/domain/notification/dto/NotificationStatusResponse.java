package in.koreatech.koin.global.domain.notification.dto;

import java.util.Arrays;
import java.util.List;

import in.koreatech.koin.global.domain.notification.model.NotificationSubscribe;
import in.koreatech.koin.global.domain.notification.model.NotificationSubscribeType;
import io.swagger.v3.oas.annotations.media.Schema;

public record NotificationStatusResponse(
    @Schema(description = "푸쉬 알림 동의 여부")
    boolean isPermit,

    @Schema(description = "푸쉬 알림 구독 목록")
    List<NotificationSubscribePermitRequest> subscribes
) {

    public record NotificationSubscribePermitRequest(
        @Schema(description = "구독 타입")
        String type,

        @Schema(description = "푸쉬 알림 동의 여부")
        boolean isPermit
    ) {

    }

    public static NotificationStatusResponse of(boolean isPermit, List<NotificationSubscribe> subscribes) {
        var results = Arrays.stream(NotificationSubscribeType.values())
            .map(type -> new NotificationSubscribePermitRequest(
                type.name(),
                subscribes.stream().anyMatch(subscribe -> subscribe.getSubscribeType() == type)
            ))
            .toList();
        return new NotificationStatusResponse(isPermit, results);
    }
}
