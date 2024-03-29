package in.koreatech.koin.global.domain.notification;

import java.util.Arrays;

import com.fasterxml.jackson.annotation.JsonCreator;

import in.koreatech.koin.global.domain.notification.exception.SubscribeNotFoundException;

public enum NotificationSubscribeType {
    ALL,
    SHOP_EVENT,
    DINING_SOLDOUT,
    ;

    @JsonCreator
    public static NotificationSubscribeType from(String type) {
        return Arrays.stream(values())
            .filter(it -> it.name().equalsIgnoreCase(type))
            .findAny()
            .orElseThrow(() -> SubscribeNotFoundException.withDetail("type: " + type));
    }
}
