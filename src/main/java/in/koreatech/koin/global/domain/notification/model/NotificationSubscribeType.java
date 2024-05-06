package in.koreatech.koin.global.domain.notification.model;

import static in.koreatech.koin.global.domain.notification.model.NotificationDetailSubscribeType.*;

import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;

import in.koreatech.koin.global.domain.notification.exception.SubscribeNotFoundException;
import lombok.Getter;

@Getter
public enum NotificationSubscribeType {
    SHOP_EVENT(List.of()),
    DINING_SOLD_OUT(List.of(BREAKFAST, LUNCH, DINNER)),
    ;

    private final List<NotificationDetailSubscribeType> detailTypes;

    NotificationSubscribeType(List<NotificationDetailSubscribeType> detailTypes) {
        this.detailTypes = detailTypes;
    }

    @JsonCreator
    public static NotificationSubscribeType from(String type) {
        return Arrays.stream(values())
            .filter(it -> it.name().equalsIgnoreCase(type))
            .findAny()
            .orElseThrow(() -> SubscribeNotFoundException.withDetail("type: " + type));
    }
}
