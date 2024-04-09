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

    public Notification generateSoldOutNotification(
        MobileAppPath path,
        String place
    ) {
        return new Notification(
            path,
            "학식 품절 알림!",
            "%s 품절되었습니다."
                .formatted(place),
            null,
            NotificationType.MESSAGE,
            null
        );
    }
}
