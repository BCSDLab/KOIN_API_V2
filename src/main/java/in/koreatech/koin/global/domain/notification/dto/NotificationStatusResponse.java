package in.koreatech.koin.global.domain.notification.dto;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import static in.koreatech.koin.global.domain.notification.model.NotificationSubscribeType.SHOP_EVENT;

import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.global.domain.notification.model.NotificationSubscribe;
import in.koreatech.koin.global.domain.notification.model.NotificationSubscribeType;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(value = SnakeCaseStrategy.class)
public record NotificationStatusResponse(
    @Schema(description = "푸쉬 알림 동의 여부")
    boolean isPermit,

    @Schema(description = "푸쉬 알림 구독 목록")
    List<NotificationSubscribeResponse> subscribes
) {

    @JsonNaming(value = SnakeCaseStrategy.class)
    public record NotificationSubscribeResponse(
        @Schema(description = "구독 타입")
        String type,

        @Schema(description = "푸쉬 알림 동의 여부")
        boolean isPermit,

        @Schema(description = "세부 알림 구독 목록")
        List<NotificationDetailSubscribeResponse> detailSubscribes
    ) {

    }

    @JsonNaming(value = SnakeCaseStrategy.class)
    public record NotificationDetailSubscribeResponse(
        @Schema(description = "세부 구독 타입")
        String detailType,

        @Schema(description = "세부 알림 동의 여부")
        boolean isPermit
    ) {

    }

    public static NotificationStatusResponse of(boolean isPermit, List<NotificationSubscribe> subscribes) {
        List<NotificationSubscribeResponse> subscribeList = new ArrayList<>();
        boolean diningIsPermit = false;
        boolean shopIsPermit = false;
        Arrays.stream(NotificationSubscribeType.values()).forEach(notificationSubscribeType -> {
            if (notificationSubscribeType.name().equals(SHOP_EVENT)) {
                subscribeList.add(new NotificationSubscribeResponse(SHOP_EVENT, true))
            } else {

            }
        });
        return new NotificationStatusResponse(isPermit, results);
    }
}
