package in.koreatech.koin.domain.notification.eventlistener;

import static in.koreatech.koin.common.model.MobileAppPath.KEYWORD;
import static in.koreatech.koin.domain.notification.model.NotificationSubscribeType.ARTICLE_KEYWORD;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import in.koreatech.koin.common.event.ArticleKeywordEvent;
import in.koreatech.koin.domain.community.article.model.Article;
import in.koreatech.koin.domain.community.article.model.Board;
import in.koreatech.koin.domain.community.article.repository.ArticleRepository;
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
        Map<Integer, String> matchedKeywordByUserId = event.matchedKeywordByUserId();

        if (matchedKeywordByUserId.isEmpty()) {
            return;
        }

        Map<Integer, NotificationSubscribe> keywordSubscribersByUserId = notificationSubscribeRepository
            .findAllBySubscribeTypeAndDetailTypeIsNullWithUser(ARTICLE_KEYWORD)
            .stream()
            .filter(this::hasDeviceToken)
            .collect(Collectors.toMap(
                subscribe -> subscribe.getUser().getId(),
                Function.identity(),
                (existing, ignored) -> existing,
                LinkedHashMap::new
            ));

        List<Notification> notifications = keywordSubscribersByUserId.values().stream()
            .filter(subscribe -> matchedKeywordByUserId.containsKey(subscribe.getUser().getId()))
            .filter(subscribe -> isNewNotifiedArticleId(event.articleId(), subscribe))
            .filter(subscribe -> !isMyArticle(event, subscribe))
            .map(subscribe -> createAndRecordNotification(
                article,
                board,
                matchedKeywordByUserId.get(subscribe.getUser().getId()),
                subscribe
            ))
            .toList();

        notificationService.pushNotifications(notifications);
    }

    private boolean hasDeviceToken(NotificationSubscribe subscribe) {
        return subscribe.getUser().getDeviceToken() != null;
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
        String keyword,
        NotificationSubscribe subscribe
    ) {
        Integer userId = subscribe.getUser().getId();
        String description = generateDescription(keyword);

        Notification notification = notificationFactory.generateKeywordNotification(
            KEYWORD,
            article.getId(),
            keyword,
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
