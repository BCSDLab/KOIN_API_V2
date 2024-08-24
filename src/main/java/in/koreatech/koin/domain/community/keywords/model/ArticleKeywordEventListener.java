package in.koreatech.koin.domain.community.keywords.model;

import static in.koreatech.koin.global.domain.notification.model.NotificationSubscribeType.ARTICLE_KEYWORD_DETECT;
import static in.koreatech.koin.global.fcm.MobileAppPath.KEYWORD;
import static org.springframework.transaction.event.TransactionPhase.AFTER_COMMIT;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.global.domain.notification.model.Notification;
import in.koreatech.koin.global.domain.notification.model.NotificationFactory;
import in.koreatech.koin.global.domain.notification.model.NotificationSubscribeType;
import in.koreatech.koin.global.domain.notification.repository.NotificationSubscribeRepository;
import in.koreatech.koin.global.domain.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
@RequiredArgsConstructor
@Transactional(propagation = Propagation.REQUIRES_NEW)
public class ArticleKeywordEventListener {

    private static final Logger log = LoggerFactory.getLogger(ArticleKeywordEventListener.class);
    private final NotificationService notificationService;
    private final NotificationFactory notificationFactory;
    private final NotificationSubscribeRepository notificationSubscribeRepository;

    @TransactionalEventListener(phase = AFTER_COMMIT)
    public void onKeywordDetectedRequest(ArticleKeywordDetectedEvent event) {
        log.info("Handling detected event for article ID: {}", event.articleId());
        String schemeUri = String.format("%s?id=%s", KEYWORD.name(), event.articleId());
        List<Notification> notifications = notificationSubscribeRepository
            .findAllBySubscribeTypeAndDetailType(ARTICLE_KEYWORD_DETECT, null)
            .stream()
            .filter(subscribe -> subscribe.getUser().getDeviceToken() != null)
            .filter(subscribe -> event.keyword().getArticleKeywordUserMaps().stream()
                .anyMatch(map -> map.getUser().getId().equals(subscribe.getUser().getId())))
            .map(subscribe -> notificationFactory.generateDetectKeywordNotification(
                KEYWORD,
                schemeUri,
                event.keyword().getKeyword(),
                subscribe.getUser()
            )).toList();

        notificationService.push(notifications);
    }
}
