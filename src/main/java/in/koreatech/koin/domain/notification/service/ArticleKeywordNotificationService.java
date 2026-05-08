package in.koreatech.koin.domain.notification.service;

import static in.koreatech.koin.common.model.MobileAppPath.KEYWORD;
import static in.koreatech.koin.domain.notification.model.NotificationSubscribeType.ARTICLE_KEYWORD;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

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
        if (userIdsByKeyword.isEmpty()) {
            return;
        }

        List<Integer> matchedUserIds = userIdsByKeyword.values().stream()
            .flatMap(List::stream)
            .distinct()
            .toList();
        if (matchedUserIds.isEmpty()) {
            return;
        }

        Map<Integer, NotificationSubscribe> subscribesByUserId = notificationSubscribeRepository
            .findArticleKeywordSubscribesByUserIdIn(ARTICLE_KEYWORD, matchedUserIds)
            .stream()
            .collect(Collectors.toMap(
                subscribe -> subscribe.getUser().getId(),
                Function.identity(),
                (existing, ignored) -> existing,
                LinkedHashMap::new
            ));

        Set<Integer> alreadyNotifiedUserIds = notificationRepository
            .findUserIdsBySchemeUriLikeAndUserIdIn("keyword?id=%d&%%".formatted(event.articleId()), matchedUserIds)
            .stream()
            .collect(Collectors.toSet());

        List<Notification> notifications = userIdsByKeyword.entrySet().stream()
            .flatMap(entry -> entry.getValue().stream()
                .filter(userId -> !alreadyNotifiedUserIds.contains(userId))
                .map(subscribesByUserId::get)
                .filter(Objects::nonNull)
                .map(subscribe -> createNotification(event, entry.getKey(), subscribe)))
            .toList();

        notificationService.pushNotificationsWithResult(notifications);
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
