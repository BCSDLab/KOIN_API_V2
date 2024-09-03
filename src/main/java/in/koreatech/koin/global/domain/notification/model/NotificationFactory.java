package in.koreatech.koin.global.domain.notification.model;

import org.springframework.stereotype.Component;

import in.koreatech.koin.domain.user.model.Device;
import in.koreatech.koin.global.fcm.MobileAppPath;

@Component
public class NotificationFactory {

    public Notification generateShopEventCreateNotification(
        MobileAppPath path,
        String schemeUri,
        String imageUrl,
        String shopName,
        String title,
        Device device
    ) {
        return new Notification(
            path,
            schemeUri,
            "%s의 이벤트가 추가되었어요 🎉".formatted(shopName),
            "%s".formatted(title),
            imageUrl,
            NotificationType.MESSAGE,
            device
        );
    }

    public Notification generateSoldOutNotification(
        MobileAppPath path,
        String schemeUri,
        String place,
        Device device
    ) {
        return new Notification(
            path,
            schemeUri,
            "%s 품절됐어요 \uD83D\uDE22".formatted(getPostposition(place, "이", "가")),
            "다른 코너 메뉴도 확인해보세요",
            null,
            NotificationType.MESSAGE,
            device
        );
    }

    private String getPostposition(String place, String firstPost, String secondPost) {
        char lastChar = place.charAt(place.length() - 1);
        String result = (lastChar - 0xAC00) % 28 > 0 ? firstPost : secondPost;
        return place + result;
    }

    public Notification generateDiningImageUploadNotification(
        MobileAppPath path,
        String schemeUri,
        String imageUrl,
        Device device
    ) {
        return new Notification(
            path,
            schemeUri,
            "식단 사진이 업로드 됐어요!",
            "사진 보러 가기 \uD83D\uDE0B",
            imageUrl,
            NotificationType.MESSAGE,
            device
        );
    }

    public Notification generateKeywordNotification(
        MobileAppPath path,
        String schemeUri,
        String keywordName,
        Device device
    ) {
        return new Notification(
            path,
            schemeUri,
            "공지사항이 등록됐어요!",
            "%s 공지가 등록되었습니다.".formatted(keywordName),
            null,
            NotificationType.MESSAGE,
            device
        );
    }
}
