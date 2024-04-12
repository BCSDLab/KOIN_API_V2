package in.koreatech.koin.global.domain.notification.model;

import org.springframework.stereotype.Component;

import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.global.fcm.MobileAppPath;

@Component
public class NotificationFactory {

    public Notification generateShopEventCreateNotification(
        MobileAppPath path,
        String shopName,
        String title,
        User target
    ) {
        return new Notification(
            path,
            "%s의 이벤트가 추가되었어요 🎉".formatted(shopName),
            "%s".formatted(title),
            null,
            NotificationType.MESSAGE,
            target
        );
    }

    public Notification generateSoldOutNotification(
        MobileAppPath path,
        String place,
        User target
    ) {
        return new Notification(
            path,
            "학식 품절 알림!",
            "%s 품절되었습니다."
                .formatted(place),
            null,
            NotificationType.MESSAGE,
            target
        );
    }
}
