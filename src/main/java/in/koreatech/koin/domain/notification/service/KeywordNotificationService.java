package in.koreatech.koin.domain.notification.service;

import static in.koreatech.koin.common.model.MobileAppPath.KEYWORD;
import static in.koreatech.koin.common.model.MobileAppPath.LOST_ITEM;
import static in.koreatech.koin.domain.notification.model.NotificationSubscribeType.ARTICLE_KEYWORD;
import static in.koreatech.koin.domain.notification.model.NotificationSubscribeType.LOST_ITEM_KEYWORD;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;

import in.koreatech.koin.common.event.KoreatechArticleKeywordEvent;
import in.koreatech.koin.common.event.LostItemKeywordEvent;
import in.koreatech.koin.domain.notification.model.Notification;
import in.koreatech.koin.domain.notification.model.NotificationFactory;
import in.koreatech.koin.domain.notification.model.NotificationSubscribe;
import in.koreatech.koin.domain.notification.repository.NotificationSubscribeRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class KeywordNotificationService {

    private final NotificationFactory notificationFactory;
    private final NotificationSubscribeRepository notificationSubscribeRepository;
    private final NotificationService notificationService;

    public void notifyArticleKeyword(KoreatechArticleKeywordEvent event) {
        List<Integer> matchedUserIds = event.matchedKeywordUsers().getMatchedUserIds();
        if (matchedUserIds.isEmpty()) {
            return;
        }

        List<NotificationSubscribe> subscribes = notificationSubscribeRepository
            .findArticleKeywordSubscribesByUserIdIn(ARTICLE_KEYWORD, matchedUserIds);
        List<Notification> notifications = createArticleNotifications(event, subscribes);

        notificationService.pushNotificationsWithResult(notifications);
    }

    public void notifyLostItemKeyword(LostItemKeywordEvent event) {
        List<Integer> matchedUserIds = event.matchedKeywordUsers().getMatchedUserIds();
        if (matchedUserIds.isEmpty()) {
            return;
        }

        List<NotificationSubscribe> subscribes = notificationSubscribeRepository
            .findArticleKeywordSubscribesByUserIdIn(LOST_ITEM_KEYWORD, matchedUserIds);
        List<Notification> notifications = createLostItemNotifications(event, subscribes);

        notificationService.pushNotificationsWithResult(notifications);
    }

    private List<Notification> createArticleNotifications(
        KoreatechArticleKeywordEvent event,
        List<NotificationSubscribe> subscribes
    ) {
        List<Notification> notifications = new ArrayList<>();
        for (NotificationSubscribe subscribe : subscribes) {
            String keyword = event.matchedKeywordUsers().getKeyword(subscribe.getUser().getId());
            notifications.add(createArticleNotification(event, keyword, subscribe));
        }
        return notifications;
    }

    private List<Notification> createLostItemNotifications(
        LostItemKeywordEvent event,
        List<NotificationSubscribe> subscribes
    ) {
        List<Notification> notifications = new ArrayList<>();
        for (NotificationSubscribe subscribe : subscribes) {
            Integer userId = subscribe.getUser().getId();
            if (Objects.equals(event.authorId(), userId)) {
                continue;
            }

            String keyword = event.matchedKeywordUsers().getKeyword(userId);
            notifications.add(createLostItemNotification(event, keyword, subscribe));
        }
        return notifications;
    }

    private Notification createArticleNotification(
        KoreatechArticleKeywordEvent event,
        String keyword,
        NotificationSubscribe subscribe
    ) {
        return notificationFactory.generateKeywordNotification(
            KEYWORD,
            event.articleId(),
            keyword,
            event.articleTitle(),
            event.boardId(),
            subscribe.getUser()
        );
    }

    private Notification createLostItemNotification(
        LostItemKeywordEvent event,
        String keyword,
        NotificationSubscribe subscribe
    ) {
        return notificationFactory.generateLostItemKeywordNotification(
            LOST_ITEM,
            event.articleId(),
            keyword,
            event.articleTitle(),
            subscribe.getUser()
        );
    }
}
