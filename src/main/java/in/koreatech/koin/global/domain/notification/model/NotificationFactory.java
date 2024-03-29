package in.koreatech.koin.global.domain.notification.model;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import in.koreatech.koin.domain.user.model.User;

@Component
public class NotificationFactory {

    private final String koinAppUrl;

    public NotificationFactory(@Value("${fcm.koin.url}") String koinAppUrl) {
        this.koinAppUrl = koinAppUrl;
    }

    public Notification generateOwnerNotification(
        String shopName,
        User target
    ) {
        return new Notification(
            koinAppUrl,
            "새로운 이벤트가 개설되었어요!",
            "%s 가게의 이벤트가 오픈되었어요!🎁"
                .formatted(shopName),
            null,
            NotificationType.MESSAGE,
            target
        );
    }
}
