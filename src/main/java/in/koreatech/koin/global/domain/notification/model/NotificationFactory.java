package in.koreatech.koin.global.domain.notification.model;

import org.springframework.stereotype.Component;

import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.global.fcm.MobileAppPath;

@Component
public class NotificationFactory {

    public Notification generateShopEventCreateNotification(
        MobileAppPath path,
        String imageUrl,
        String shopName,
        String title,
        User target
    ) {
        return new Notification(
            path,
            "%s의 이벤트가 추가되었어요 🎉".formatted(shopName),
            "%s".formatted(title),
            imageUrl,
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
            "%s 품절되었습니다.".formatted(getPostposition(place, "이", "가")),
            "다른 식단 보러 가기.",
            null,
            NotificationType.MESSAGE,
            target
        );
    }

    private String getPostposition(String place, String firstPost, String secondPost){
        char lastChar = place.charAt(place.length() - 1);
        String result = (lastChar - 0xAC00) % 28 > 0 ? firstPost : secondPost;
        return place + result;
    }

    public Notification generateDiningImageUploadNotification(
        MobileAppPath path,
        String imageUrl,
        User target
    ){
        return new Notification(
            path,
            "학식 사진이 업로드되었습니다!",
            "사진 보러가기",
            imageUrl,
            NotificationType.MESSAGE,
            target
        );
    }
}
