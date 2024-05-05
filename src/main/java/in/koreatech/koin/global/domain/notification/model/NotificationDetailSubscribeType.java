package in.koreatech.koin.global.domain.notification.model;

import static in.koreatech.koin.global.domain.notification.model.NotificationSubscribeType.DINING_SOLD_OUT;

import java.util.Arrays;

import com.fasterxml.jackson.annotation.JsonCreator;

import in.koreatech.koin.global.domain.notification.exception.SubscribeNotFoundException;
import lombok.Getter;

@Getter
public enum NotificationDetailSubscribeType {
    BREAKFAST(DINING_SOLD_OUT),
    LUNCH(DINING_SOLD_OUT),
    DINNER(DINING_SOLD_OUT),
    ;

    private final NotificationSubscribeType subscribeType;

    NotificationDetailSubscribeType(NotificationSubscribeType subscribeType) {
        this.subscribeType = subscribeType;
    }

    @JsonCreator
    public static NotificationDetailSubscribeType from(String detailType) {
        return Arrays.stream(values())
            .filter(it -> it.name().equalsIgnoreCase(detailType))
            .findAny()
            .orElseThrow(() -> SubscribeNotFoundException.withDetail("detailType: " + detailType));
    }

}
