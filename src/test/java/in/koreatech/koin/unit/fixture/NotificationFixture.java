package in.koreatech.koin.unit.fixture;

import static in.koreatech.koin.common.model.MobileAppPath.KEYWORD;
import static in.koreatech.koin.domain.notification.model.NotificationSubscribeType.ARTICLE_KEYWORD;

import in.koreatech.koin.domain.notification.model.Notification;
import in.koreatech.koin.domain.notification.model.NotificationSubscribe;
import in.koreatech.koin.domain.user.model.User;

public final class NotificationFixture {

    private NotificationFixture() {}

    public static NotificationSubscribe 공지_키워드_구독(User user) {
        return NotificationSubscribe.builder()
            .subscribeType(ARTICLE_KEYWORD)
            .user(user)
            .build();
    }

    public static Notification 키워드_알림(User user) {
        return Notification.of(
            KEYWORD,
            "koin://keyword",
            "수강신청 안내",
            "방금 등록된 수강신청 공지를 확인해보세요!",
            null,
            user
        );
    }
}
