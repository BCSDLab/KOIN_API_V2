package in.koreatech.koin.domain.notification.dto;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;

import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.notification.model.NotificationDetailSubscribeType;
import in.koreatech.koin.domain.notification.model.NotificationSubscribe;
import in.koreatech.koin.domain.notification.model.NotificationSubscribeType;
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

        @Schema(description = "구독 여부")
        boolean isPermit,

        @Schema(description = "구독 목록")
        List<NotificationDetailSubscribeResponse> detailSubscribes
    ) {

    }

    @JsonNaming(value = SnakeCaseStrategy.class)
    public record NotificationDetailSubscribeResponse(
        @Schema(description = "세부 구독 타입")
        String detailType,

        @Schema(description = "세부 구독 여부")
        boolean isPermit
    ) {

    }

    public static NotificationStatusResponse of(
        boolean isPermit,
        List<NotificationSubscribe> subscribeList
    ) {
        List<NotificationSubscribe> subscribes = subscribeList.stream()
            .filter(subscribe -> subscribe.getDetailType() == null)
            .toList();
        List<NotificationSubscribe> detailSubscribes = subscribeList.stream()
            .filter(subscribe -> subscribe.getDetailType() != null)
            .toList();

        var subscribeResponses = Arrays.stream(NotificationSubscribeType.values())
            .map(type -> new NotificationSubscribeResponse(
                type.name(),
                subscribes.stream().anyMatch(subscribe -> subscribe.getSubscribeType() == type),
                Arrays.stream(type.getDetailTypes().toArray(NotificationDetailSubscribeType[]::new))
                    .map(detailType -> new NotificationDetailSubscribeResponse(
                        detailType.name(),
                        detailSubscribes.stream()
                            .anyMatch(detailSubscribe -> detailSubscribe.getDetailType() == detailType)
                    )).toList()
            )).toList();
        return new NotificationStatusResponse(isPermit, subscribeResponses);
    }
}
