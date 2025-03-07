package in.koreatech.koin.integration.fcm.notification.controller;

import java.util.Arrays;

import org.springframework.core.convert.converter.Converter;

import in.koreatech.koin.integration.fcm.notification.exception.NotificationSubscribeNotFoundException;
import in.koreatech.koin.integration.fcm.notification.model.NotificationSubscribeType;

public class NotificationSubscribeTypeConverter implements Converter<String, NotificationSubscribeType> {

    @Override
    public NotificationSubscribeType convert(String source) {
        return Arrays.stream(NotificationSubscribeType.values())
            .filter(it -> it.name().equalsIgnoreCase(source))
            .findAny()
            .orElseThrow(
                () -> NotificationSubscribeNotFoundException.withDetail("NotificationSubscribeType: " + source));
    }
}
