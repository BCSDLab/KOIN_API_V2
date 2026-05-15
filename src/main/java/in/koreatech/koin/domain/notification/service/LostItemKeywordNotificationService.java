package in.koreatech.koin.domain.notification.service;

import static in.koreatech.koin.common.model.MobileAppPath.LOST_ITEM;
import static in.koreatech.koin.domain.notification.model.NotificationSubscribeType.LOST_ITEM_KEYWORD;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.springframework.stereotype.Service;

import in.koreatech.koin.common.event.LostItemKeywordEvent;
import in.koreatech.koin.domain.notification.model.Notification;
import in.koreatech.koin.domain.notification.model.NotificationFactory;
import in.koreatech.koin.domain.notification.model.NotificationSubscribe;
import in.koreatech.koin.domain.notification.repository.NotificationRepository;
import in.koreatech.koin.domain.notification.repository.NotificationSubscribeRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LostItemKeywordNotificationService {

    private final NotificationFactory notificationFactory;
    private final NotificationSubscribeRepository notificationSubscribeRepository;
    private final NotificationRepository notificationRepository;
    private final NotificationService notificationService;

    public void notifyLostItemKeyword(LostItemKeywordEvent event) {
        Map<String, List<Integer>> userIdsByKeyword = event.matchedKeywordUsers().userIdsByKeyword();
        List<Integer> matchedUserIds = getMatchedUserIds(userIdsByKeyword);
        if (matchedUserIds.isEmpty()) {
            return;
        }

        Map<Integer, NotificationSubscribe> subscribesByUserId = findSubscribesByUserId(matchedUserIds);
        Set<Integer> alreadyNotifiedUserIds = getAlreadyNotifiedUserIds(event.articleId(), matchedUserIds);
        List<Notification> notifications = createNotifications(event, userIdsByKeyword, subscribesByUserId,
            alreadyNotifiedUserIds);

        notificationService.pushNotificationsWithResult(notifications);
    }

    private List<Integer> getMatchedUserIds(Map<String, List<Integer>> userIdsByKeyword) {
        Set<Integer> userIds = new HashSet<>();
        for (List<Integer> keywordUserIds : userIdsByKeyword.values()) {
            userIds.addAll(keywordUserIds);
        }
        return new ArrayList<>(userIds);
    }

    private Map<Integer, NotificationSubscribe> findSubscribesByUserId(List<Integer> userIds) {
        Map<Integer, NotificationSubscribe> subscribesByUserId = new LinkedHashMap<>();
        List<NotificationSubscribe> subscribes = notificationSubscribeRepository
            .findArticleKeywordSubscribesByUserIdIn(LOST_ITEM_KEYWORD, userIds);

        for (NotificationSubscribe subscribe : subscribes) {
            Integer userId = subscribe.getUser().getId();
            subscribesByUserId.putIfAbsent(userId, subscribe);
        }
        return subscribesByUserId;
    }

    private Set<Integer> getAlreadyNotifiedUserIds(Integer articleId, List<Integer> userIds) {
        String schemeUriPattern = "%s?id=%d&%%".formatted(LOST_ITEM.getPath(), articleId);
        return new HashSet<>(notificationRepository.findUserIdsBySchemeUriLikeAndUserIdIn(schemeUriPattern, userIds));
    }

    private List<Notification> createNotifications(
        LostItemKeywordEvent event,
        Map<String, List<Integer>> userIdsByKeyword,
        Map<Integer, NotificationSubscribe> subscribesByUserId,
        Set<Integer> alreadyNotifiedUserIds
    ) {
        List<Notification> notifications = new ArrayList<>();
        for (Map.Entry<String, List<Integer>> entry : userIdsByKeyword.entrySet()) {
            String keyword = entry.getKey();
            for (Integer userId : entry.getValue()) {
                if (alreadyNotifiedUserIds.contains(userId)) {
                    continue;
                }

                if (isMyArticle(event, userId)) {
                    continue;
                }

                NotificationSubscribe subscribe = subscribesByUserId.get(userId);
                if (subscribe == null) {
                    continue;
                }

                notifications.add(createNotification(event, keyword, subscribe));
            }
        }
        return notifications;
    }

    private boolean isMyArticle(LostItemKeywordEvent event, Integer subscriberId) {
        return Objects.equals(event.authorId(), subscriberId);
    }

    private Notification createNotification(
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
