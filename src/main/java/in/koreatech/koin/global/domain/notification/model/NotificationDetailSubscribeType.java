package in.koreatech.koin.global.domain.notification.model;

import static in.koreatech.koin.global.domain.notification.model.NotificationSubscribeType.DINING_SOLD_OUT;

import java.util.Arrays;

import com.fasterxml.jackson.annotation.JsonCreator;

import in.koreatech.koin.domain.dining.model.DiningType;
import in.koreatech.koin.global.domain.notification.exception.SubscribeNotFoundException;
import lombok.Getter;

@Getter
public enum NotificationDetailSubscribeType {
    BREAKFAST,
    LUNCH,
    DINNER,
    ;

    @JsonCreator
    public static NotificationDetailSubscribeType from(String detailType) {
        return Arrays.stream(values())
            .filter(it -> it.name().equalsIgnoreCase(detailType))
            .findAny()
            .orElseThrow(() -> SubscribeNotFoundException.withDetail("detailType: " + detailType));
    }

    public static NotificationSubscribeType getSubscribeType(NotificationDetailSubscribeType detailType) {
        if (detailType == BREAKFAST || detailType == LUNCH || detailType == DINNER) {
            return DINING_SOLD_OUT;
        }
        return null;
    }

    public static NotificationDetailSubscribeType from(DiningType diningType) {
        return NotificationDetailSubscribeType.valueOf(diningType.name());
    }
}
