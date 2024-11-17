package in.koreatech.koin.global.domain.notification.model;

import org.springframework.stereotype.Component;

import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.global.fcm.MobileAppPath;

@Component
public class NotificationFactory {

    public Notification generateReviewPromptNotification(
        MobileAppPath path,
        Integer eventShopId,
        String shopName,
        String title,
        String message,
        User target
    ) {
        return new Notification(
            path,
            generateSchemeUri(path, eventShopId),
            String.format("%s%s", shopName, title),
            message,
            null,
            NotificationType.MESSAGE,
            target
        );
    }

    public Notification generateShopEventCreateNotification(
        MobileAppPath path,
        Integer eventShopId,
        String imageUrl,
        String shopName,
        String title,
        User target
    ) {
        return new Notification(
            path,
            generateSchemeUri(path, eventShopId),
            "%s의 이벤트가 추가되었어요 🎉".formatted(shopName),
            "%s".formatted(title),
            imageUrl,
            NotificationType.MESSAGE,
            target
        );
    }

    public Notification generateSoldOutNotification(
        MobileAppPath path,
        Integer eventDiningId,
        String place,
        User target
    ) {
        return new Notification(
            path,
            generateSchemeUri(path, eventDiningId),
            "%s 품절됐어요 \uD83D\uDE22".formatted(getPostposition(place, "이", "가")),
            "다른 코너 메뉴도 확인해보세요",
            null,
            NotificationType.MESSAGE,
            target
        );
    }

    public Notification generateDiningImageUploadNotification(
        MobileAppPath path,
        Integer eventDiningId,
        String imageUrl,
        User target
    ) {
        return new Notification(
            path,
            generateSchemeUri(path, eventDiningId),
            "식단 사진이 업로드 됐어요!",
            "사진 보러 가기 \uD83D\uDE0B",
            imageUrl,
            NotificationType.MESSAGE,
            target
        );
    }

    public Notification generateKeywordNotification(
        MobileAppPath path,
        Integer eventKeywordId,
        String title,
        String description,
        User target
    ) {
        return new Notification(
            path,
            generateSchemeUri(path, eventKeywordId),
            title,
            description,
            null,
            NotificationType.MESSAGE,
            target
        );
    }

    private String generateSchemeUri(MobileAppPath path, Integer eventId) {
        if (eventId == null) {
            return path.getPath();
        }
        return String.format("%s?id=%d", path.getPath(), eventId);
    }

    private String getPostposition(String place, String firstPost, String secondPost) {
        char lastChar = place.charAt(place.length() - 1);
        String result = (lastChar - 0xAC00) % 28 > 0 ? firstPost : secondPost;
        return place + result;
    }
}
