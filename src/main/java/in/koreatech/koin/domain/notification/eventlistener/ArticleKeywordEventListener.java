package in.koreatech.koin.domain.notification.eventlistener;

import static in.koreatech.koin.common.model.MobileAppPath.KEYWORD;
import static in.koreatech.koin.domain.notification.model.NotificationSubscribeType.ARTICLE_KEYWORD;

import java.util.List;
import java.util.Objects;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import in.koreatech.koin.common.event.ArticleKeywordEvent;
import in.koreatech.koin.domain.community.article.model.Article;
import in.koreatech.koin.domain.community.article.model.Board;
import in.koreatech.koin.domain.community.article.repository.ArticleRepository;
import in.koreatech.koin.domain.community.keyword.model.ArticleKeyword;
import in.koreatech.koin.domain.community.keyword.repository.UserNotificationStatusRepository;
import in.koreatech.koin.domain.community.keyword.service.KeywordService;
import in.koreatech.koin.domain.notification.model.Notification;
import in.koreatech.koin.domain.notification.model.NotificationFactory;
import in.koreatech.koin.domain.notification.model.NotificationSubscribe;
import in.koreatech.koin.domain.notification.repository.NotificationSubscribeRepository;
import in.koreatech.koin.domain.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Profile("!test")
public class ArticleKeywordEventListener { // TODO : 리팩터링 필요 (비즈니스로직 제거 및 알림 책임만 갖도록)

    private final NotificationService notificationService;
    private final NotificationFactory notificationFactory;
    private final NotificationSubscribeRepository notificationSubscribeRepository;
    private final UserNotificationStatusRepository userNotificationStatusRepository;
    private final KeywordService keywordService;
    private final ArticleRepository articleRepository;

    @TransactionalEventListener
    public void onKeywordRequest(ArticleKeywordEvent event) {
        Article article = articleRepository.getById(event.articleId());
        Board board = article.getBoard();

        List<Notification> notifications = notificationSubscribeRepository
            .findAllBySubscribeTypeAndDetailTypeIsNull(ARTICLE_KEYWORD)
            .stream()
            .filter(this::hasDeviceToken)
            .filter(subscribe -> isKeywordRegistered(event, subscribe))
            .filter(subscribe -> isNewNotifiedArticleId(event.articleId(), subscribe))
            .filter(subscribe -> !isMyArticle(event, subscribe))
            .map(subscribe -> createAndRecordNotification(article, board, event.keyword(), subscribe))
            .toList();

        notificationService.pushNotifications(notifications);
    }

    private boolean hasDeviceToken(NotificationSubscribe subscribe) {
        return subscribe.getUser().getDeviceToken() != null;
    }

    private boolean isKeywordRegistered(ArticleKeywordEvent event, NotificationSubscribe subscribe) {
        return event.keyword().getArticleKeywordUserMaps().stream()
            .filter(map -> !map.getIsDeleted())
            .anyMatch(map -> map.getUser().getId().equals(subscribe.getUser().getId()));
    }

    private boolean isNewNotifiedArticleId(Integer articleId, NotificationSubscribe subscribe) {
        Integer userId = subscribe.getUser().getId();
        return !userNotificationStatusRepository.existsByNotifiedArticleIdAndUserId(articleId, userId);
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

        keywordService.createNotifiedArticleStatus(userId, article.getId());
        return notification;
    }

    private String generateDescription(String keyword) {
        return "방금 등록된 %s 공지를 확인해보세요!".formatted(keyword);
    }
}
