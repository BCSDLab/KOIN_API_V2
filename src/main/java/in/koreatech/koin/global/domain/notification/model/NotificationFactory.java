package in.koreatech.koin.global.domain.notification.model;

import org.springframework.stereotype.Component;

import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.global.fcm.MobileAppPath;

@Component
public class NotificationFactory {

    public Notification generateOwnerNotification(
        MobileAppPath path,
        String shopName,
        User target
    ) {
        return new Notification(
            path,
            "새로운 이벤트가 개설되었어요!",
            "%s 가게의 이벤트가 오픈되었어요!🎁"
                .formatted(shopName),
            null,
            NotificationType.MESSAGE,
            target
        );
    }
}
