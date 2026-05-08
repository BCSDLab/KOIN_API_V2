package in.koreatech.koin.domain.notification.service;

import static in.koreatech.koin.common.model.MobileAppPath.KEYWORD;
import static in.koreatech.koin.domain.notification.model.NotificationSubscribeType.ARTICLE_KEYWORD;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;

import in.koreatech.koin.common.event.KoreatechArticleKeywordEvent;
import in.koreatech.koin.domain.notification.model.Notification;
import in.koreatech.koin.domain.notification.model.NotificationFactory;
import in.koreatech.koin.domain.notification.model.NotificationSubscribe;
import in.koreatech.koin.domain.notification.repository.NotificationRepository;
import in.koreatech.koin.domain.notification.repository.NotificationSubscribeRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ArticleKeywordNotificationService {

    private final NotificationFactory notificationFactory;
    private final NotificationSubscribeRepository notificationSubscribeRepository;
    private final NotificationRepository notificationRepository;
    private final NotificationService notificationService;

    public void notifyArticleKeyword(KoreatechArticleKeywordEvent event) {
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
            .findArticleKeywordSubscribesByUserIdIn(ARTICLE_KEYWORD, userIds);

        for (NotificationSubscribe subscribe : subscribes) {
            Integer userId = subscribe.getUser().getId();
            subscribesByUserId.putIfAbsent(userId, subscribe);
        }
        return subscribesByUserId;
    }

    private Set<Integer> getAlreadyNotifiedUserIds(Integer articleId, List<Integer> userIds) {
        return new HashSet<>(notificationRepository
            .findUserIdsBySchemeUriLikeAndUserIdIn("%s?id=%d&%%".formatted(KEYWORD.getPath(), articleId), userIds));
    }

    private List<Notification> createNotifications(
        KoreatechArticleKeywordEvent event,
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

                NotificationSubscribe subscribe = subscribesByUserId.get(userId);
                if (subscribe == null) {
                    continue;
                }

                notifications.add(createNotification(event, keyword, subscribe));
            }
        }
        return notifications;
    }

    private Notification createNotification(
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
}
