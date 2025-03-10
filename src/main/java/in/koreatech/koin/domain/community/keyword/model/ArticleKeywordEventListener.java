package in.koreatech.koin.domain.community.keyword.model;

import static in.koreatech.koin.global.domain.notification.model.NotificationSubscribeType.ARTICLE_KEYWORD;
import static in.koreatech.koin.global.fcm.MobileAppPath.KEYWORD;
import static org.springframework.transaction.event.TransactionPhase.AFTER_COMMIT;

import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

import in.koreatech.koin.domain.community.article.model.Article;
import in.koreatech.koin.domain.community.article.model.Board;
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
        Board board = article.getBoard();

        List<Notification> notifications = notificationSubscribeRepository
            .findAllBySubscribeTypeAndDetailType(ARTICLE_KEYWORD, null)
            .stream()
            .filter(this::hasDeviceToken)
            .filter(subscribe -> isKeywordRegistered(event, subscribe))
            .filter(subscribe -> isNewArticle(event, subscribe))
            .filter(subscribe -> !isMyArticle(event, subscribe))
            .map(subscribe -> createAndRecordNotification(article, board, event.keyword(), subscribe))
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

    private boolean isMyArticle(ArticleKeywordEvent event, NotificationSubscribe subscribe) {
        Integer authorId = event.authorId();
        Integer subscriberId = subscribe.getUser().getId();
        return Objects.equals(authorId, subscriberId);
    }

    private Notification createAndRecordNotification(
        Article article,
        Board board,
        ArticleKeyword keyword,
        NotificationSubscribe subscribe
    ) {
        Integer userId = subscribe.getUser().getId();
        String description = generateDescription(keyword.getKeyword());

        Notification notification = notificationFactory.generateKeywordNotification(
            KEYWORD,
            article.getId(),
            keyword.getKeyword(),
            article.getTitle(),
            board.getId(),
            description,
            subscribe.getUser()
        );

        keywordService.updateLastNotifiedArticle(userId, article.getId());
        return notification;
    }

    private String generateDescription(String keyword) {
        return "방금 등록된 %s 공지를 확인해보세요!".formatted(keyword);
    }
}
