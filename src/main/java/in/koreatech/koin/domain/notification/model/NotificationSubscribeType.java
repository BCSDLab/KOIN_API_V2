package in.koreatech.koin.domain.notification.model;

import static in.koreatech.koin.domain.notification.model.NotificationDetailSubscribeType.*;

import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;

import in.koreatech.koin.domain.notification.exception.SubscribeNotFoundException;
import lombok.Getter;

@Getter
public enum NotificationSubscribeType {
    SHOP_EVENT(List.of()),
    REVIEW_PROMPT(List.of()),
    DINING_SOLD_OUT(List.of(BREAKFAST, LUNCH, DINNER)),
    DINING_IMAGE_UPLOAD(List.of()),
    ARTICLE_KEYWORD(List.of()),
    LOST_ITEM_CHAT(List.of())
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

    public static NotificationSubscribeType getParentType(NotificationDetailSubscribeType childType) {
        return Arrays.stream(values())
            .filter(parentType -> parentType.detailTypes.contains(childType))
            .findAny()
            .orElseThrow(() -> SubscribeNotFoundException.withDetail("childType: " + childType));
    }
}
