package in.koreatech.koin.domain.community.keyword.model;

import static in.koreatech.koin.domain.community.article.service.ArticleService.LOST_ITEM_BOARD_ID;
import static in.koreatech.koin.global.domain.notification.model.NotificationSubscribeType.ARTICLE_KEYWORD;
import static in.koreatech.koin.global.fcm.MobileAppPath.KEYWORD;
import static org.springframework.transaction.event.TransactionPhase.AFTER_COMMIT;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

import in.koreatech.koin.domain.community.article.model.Article;
import in.koreatech.koin.domain.community.article.repository.ArticleRepository;
import in.koreatech.koin.domain.community.keyword.repository.UserNotificationStatusRepository;
import in.koreatech.koin.domain.community.keyword.service.KeywordService;
import in.koreatech.koin.global.domain.notification.model.Notification;
import in.koreatech.koin.global.domain.notification.model.NotificationFactory;
import in.koreatech.koin.global.domain.notification.model.NotificationSubscribe;
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
    private final ArticleRepository articleRepository;

    @TransactionalEventListener(phase = AFTER_COMMIT)
    public void onKeywordRequest(ArticleKeywordEvent event) {
        Article article = articleRepository.getById(event.articleId());

        List<Notification> notifications = notificationSubscribeRepository
            .findAllBySubscribeTypeAndDetailType(ARTICLE_KEYWORD, null)
            .stream()
            .filter(this::hasDeviceToken)
            .filter(subscribe -> isKeywordRegistered(event, subscribe))
            .filter(subscribe -> isNewArticle(event, subscribe))
            .map(subscribe -> createAndRecordNotification(event, article, subscribe))
            .toList();

        notificationService.push(notifications);
    }

    private boolean hasDeviceToken(NotificationSubscribe subscribe) {
        return subscribe.getUser().getDeviceToken() != null;
    }

    private boolean isKeywordRegistered(ArticleKeywordEvent event, NotificationSubscribe subscribe) {
        return event.keyword().getArticleKeywordUserMaps().stream()
            .anyMatch(map -> map.getUser().getId().equals(subscribe.getUser().getId()));
    }

    private boolean isNewArticle(ArticleKeywordEvent event, NotificationSubscribe subscribe) {
        Integer userId = subscribe.getUser().getId();
        Integer lastNotifiedId = userNotificationStatusRepository
            .findLastNotifiedArticleIdByUserId(userId)
            .orElse(0);
        return !lastNotifiedId.equals(event.articleId());
    }

    private Notification createAndRecordNotification(
        ArticleKeywordEvent event,
        Article article,
        NotificationSubscribe subscribe
    ) {
        Integer userId = subscribe.getUser().getId();
        String keyword = event.keyword().getKeyword();
        String title;
        String message;

        if (event.boardId() == LOST_ITEM_BOARD_ID) {
            title = generateLostItemTitle(keyword);
            message = article.getContent();
        } else {
            title = article.getTitle();
            message = generateNoticeMessage(keyword);
        }

        Notification notification = notificationFactory.generateKeywordNotification(
            KEYWORD,
            event.articleId(),
            keyword,
            title,
            message,
            subscribe.getUser()
        );

        keywordService.updateLastNotifiedArticle(userId, event.articleId());
        return notification;
    }

    private String generateNoticeMessage(String keyword) {
        return "방금 등록된 %s 공지를 확인해보세요!".formatted(keyword);
    }

    private String generateLostItemTitle(String keyword) {
        return "%s 분실물을 찾았어요!".formatted(keyword);
    }
}
