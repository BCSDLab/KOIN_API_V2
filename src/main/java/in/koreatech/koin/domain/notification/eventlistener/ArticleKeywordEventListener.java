package in.koreatech.koin.domain.notification.eventlistener;

import static in.koreatech.koin.common.model.MobileAppPath.KEYWORD;
import static in.koreatech.koin.domain.community.keyword.enums.KeywordCategory.KOREATECH;
import static in.koreatech.koin.domain.community.keyword.enums.KeywordCategory.LOST_ITEM;
import static in.koreatech.koin.domain.notification.model.NotificationSubscribeType.ARTICLE_KEYWORD;
import static in.koreatech.koin.domain.notification.model.NotificationSubscribeType.LOST_ITEM_KEYWORD;
import static org.springframework.transaction.event.TransactionPhase.AFTER_COMMIT;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.util.StringUtils;

import in.koreatech.koin.common.event.ArticleKeywordEvent;
import in.koreatech.koin.common.model.MobileAppPath;
import in.koreatech.koin.domain.community.article.model.Article;
import in.koreatech.koin.domain.community.article.model.Board;
import in.koreatech.koin.domain.community.article.repository.ArticleRepository;
import in.koreatech.koin.domain.community.keyword.repository.UserNotificationStatusRepository;
import in.koreatech.koin.domain.community.keyword.service.KeywordService;
import in.koreatech.koin.domain.notification.model.Notification;
import in.koreatech.koin.domain.notification.model.NotificationFactory;
import in.koreatech.koin.domain.notification.model.NotificationSubscribe;
import in.koreatech.koin.domain.notification.model.NotificationSubscribeType;
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

    @Async(value = "keywordNotificationTaskExecutor")
    @TransactionalEventListener(phase = AFTER_COMMIT)
    public void onKeywordRequest(ArticleKeywordEvent event) {
        Map<Integer, String> matchedKeywordByUserId = event.matchedKeywordByUserId();

        if (matchedKeywordByUserId.isEmpty()) {
            return;
        }

        Article article = articleRepository.getById(event.articleId());
        Board board = article.getBoard();
        MobileAppPath appPath = getAppPath(event);

        Map<Integer, NotificationSubscribe> keywordSubscribersByUserId = notificationSubscribeRepository
            .findAllBySubscribeTypeAndDetailTypeIsNullWithUser(getSubscribeType(event))
            .stream()
            .filter(this::hasDeviceToken)
            .collect(Collectors.toMap(
                subscribe -> subscribe.getUser().getId(),
                Function.identity(),
                (existing, ignored) -> existing,
                LinkedHashMap::new
            ));

        Set<Integer> matchedUserIds = keywordSubscribersByUserId.keySet().stream()
            .filter(matchedKeywordByUserId::containsKey)
            .collect(Collectors.toSet());

        Set<Integer> alreadyNotifiedUserIds = getAlreadyNotifiedUserIds(
            event.articleId(),
            matchedUserIds
        );

        List<Notification> notifications = keywordSubscribersByUserId.values().stream()
            .filter(subscribe -> matchedUserIds.contains(subscribe.getUser().getId()))
            .filter(subscribe -> !alreadyNotifiedUserIds.contains(subscribe.getUser().getId()))
            .filter(subscribe -> !isMyArticle(event, subscribe))
            .map(subscribe -> createNotification(
                article,
                board,
                appPath,
                event,
                matchedKeywordByUserId.get(subscribe.getUser().getId()),
                subscribe
            ))
            .toList();

        List<NotificationService.NotificationDeliveryResult> deliveryResults =
            notificationService.pushNotificationsWithResult(notifications);
        for (NotificationService.NotificationDeliveryResult deliveryResult : deliveryResults) {
            if (deliveryResult.delivered()) {
                keywordService.createNotifiedArticleStatus(deliveryResult.notification().getUser().getId(),
                    article.getId());
            }
        }
    }

    private boolean hasDeviceToken(NotificationSubscribe subscribe) {
        return StringUtils.hasText(subscribe.getUser().getDeviceToken());
    }

    private NotificationSubscribeType getSubscribeType(ArticleKeywordEvent event) {
        if (event.category() == KOREATECH) {
            return ARTICLE_KEYWORD;
        }
        if (event.category() == LOST_ITEM) {
            return LOST_ITEM_KEYWORD;
        }
        throw new IllegalArgumentException("지원하지 않는 키워드 카테고리입니다: " + event.category());
    }

    private MobileAppPath getAppPath(ArticleKeywordEvent event) {
        if (event.category() == KOREATECH) {
            return KEYWORD;
        }
        if (event.category() == LOST_ITEM) {
            return MobileAppPath.LOST_ITEM;
        }
        throw new IllegalArgumentException("지원하지 않는 키워드 카테고리입니다: " + event.category());
    }

    private Set<Integer> getAlreadyNotifiedUserIds(Integer articleId, Set<Integer> subscriberUserIds) {
        if (subscriberUserIds.isEmpty()) {
            return Set.of();
        }
        return new HashSet<>(
            userNotificationStatusRepository.findUserIdsByNotifiedArticleIdAndUserIdIn(articleId, subscriberUserIds)
        );
    }

    private boolean isMyArticle(ArticleKeywordEvent event, NotificationSubscribe subscribe) {
        Integer authorId = event.authorId();
        Integer subscriberId = subscribe.getUser().getId();
        return Objects.equals(authorId, subscriberId);
    }

    private Notification createNotification(
        Article article,
        Board board,
        MobileAppPath appPath,
        ArticleKeywordEvent event,
        String keyword,
        NotificationSubscribe subscribe
    ) {
        String description = generateDescription(event, keyword);

        return notificationFactory.generateKeywordNotification(
            appPath,
            article.getId(),
            keyword,
            article.getTitle(),
            board.getId(),
            description,
            subscribe.getUser()
        );
    }

    private String generateDescription(ArticleKeywordEvent event, String keyword) {
        if (event.category() == LOST_ITEM) {
            return "방금 등록된 %s 분실물 게시물을 확인해보세요!".formatted(keyword);
        }
        return "방금 등록된 %s 공지를 확인해보세요!".formatted(keyword);
    }
}
