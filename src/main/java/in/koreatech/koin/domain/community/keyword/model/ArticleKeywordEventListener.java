package in.koreatech.koin.domain.community.keyword.model;

import static in.koreatech.koin.global.domain.notification.model.NotificationSubscribeType.ARTICLE_KEYWORD;
import static in.koreatech.koin.global.fcm.MobileAppPath.KEYWORD;
import static org.springframework.transaction.event.TransactionPhase.AFTER_COMMIT;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

import in.koreatech.koin.domain.community.keyword.repository.UserNotificationStatusRepository;
import in.koreatech.koin.domain.community.keyword.service.KeywordService;
import in.koreatech.koin.global.domain.notification.model.Notification;
import in.koreatech.koin.global.domain.notification.model.NotificationFactory;
import in.koreatech.koin.global.domain.notification.repository.NotificationSubscribeRepository;
import in.koreatech.koin.global.domain.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Transactional(propagation = Propagation.REQUIRES_NEW)
public class ArticleKeywordEventListener {

    private final NotificationService notificationService;
    private final NotificationFactory notificationFactory;
    private final NotificationSubscribeRepository notificationSubscribeRepository;
    private final UserNotificationStatusRepository userNotificationStatusRepository;
    private final KeywordService keywordService;


    @TransactionalEventListener(phase = AFTER_COMMIT)
    public void onKeywordRequest(ArticleKeywordEvent event) {
        List<Notification> notifications = notificationSubscribeRepository
            .findAllBySubscribeTypeAndDetailType(ARTICLE_KEYWORD, null)
            .stream()
            .filter(subscribe -> subscribe.getUser().getDeviceToken() != null)
            .filter(subscribe -> event.keyword().getArticleKeywordUserMaps().stream()
                .anyMatch(map -> map.getUser().getId().equals(subscribe.getUser().getId())))
            .filter(subscribe -> {
                Integer userId = subscribe.getUser().getId();
                Integer lastNotifiedId = userNotificationStatusRepository.findLastNotifiedArticleIdByUserId(userId)
                    .orElse(0);
                return lastNotifiedId == event.articleId();
            })
            .map(subscribe -> {
                Integer userId = subscribe.getUser().getId();
                Notification notification = notificationFactory.generateKeywordNotification(
                    KEYWORD,
                    event.articleId(),
                    event.keyword().getKeyword(),
                    subscribe.getUser()
                );

                keywordService.updateLastNotifiedArticle(userId, event.articleId());
                return notification;
            })
            .toList();

        notificationService.push(notifications);
    }
}
