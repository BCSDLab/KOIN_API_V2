package in.koreatech.koin.unit.domain.notification.eventlistener;

import static in.koreatech.koin.common.model.MobileAppPath.KEYWORD;
import static in.koreatech.koin.domain.notification.model.NotificationSubscribeType.ARTICLE_KEYWORD;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import in.koreatech.koin.common.event.ArticleKeywordEvent;
import in.koreatech.koin.domain.community.article.model.Article;
import in.koreatech.koin.domain.community.article.model.Board;
import in.koreatech.koin.domain.community.article.repository.ArticleRepository;
import in.koreatech.koin.domain.community.keyword.repository.UserNotificationStatusRepository;
import in.koreatech.koin.domain.community.keyword.service.KeywordService;
import in.koreatech.koin.domain.notification.eventlistener.ArticleKeywordEventListener;
import in.koreatech.koin.domain.notification.model.Notification;
import in.koreatech.koin.domain.notification.model.NotificationFactory;
import in.koreatech.koin.domain.notification.model.NotificationSubscribe;
import in.koreatech.koin.domain.notification.repository.NotificationSubscribeRepository;
import in.koreatech.koin.domain.notification.service.NotificationService;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.unit.fixture.UserFixture;

@ExtendWith(MockitoExtension.class)
class ArticleKeywordEventListenerTest {

    @InjectMocks
    private ArticleKeywordEventListener articleKeywordEventListener;

    @Mock
    private NotificationService notificationService;

    @Mock
    private NotificationFactory notificationFactory;

    @Mock
    private NotificationSubscribeRepository notificationSubscribeRepository;

    @Mock
    private UserNotificationStatusRepository userNotificationStatusRepository;

    @Mock
    private KeywordService keywordService;

    @Mock
    private ArticleRepository articleRepository;

    @Test
    @DisplayName("중복 구독이 있어도 사용자당 알림은 한 번만 발송된다.")
    void onKeywordRequest_withDuplicateSubscriptions_sendsSingleNotification() {
        Integer articleId = 100;
        Integer boardId = 12;
        Integer userId = 1;
        User subscriber = UserFixture.id_설정_코인_유저(userId);
        subscriber.permitNotification("device-token");

        NotificationSubscribe subscribeA = createKeywordSubscribe(subscriber);
        NotificationSubscribe subscribeB = createKeywordSubscribe(subscriber);
        ArticleKeywordEvent event = new ArticleKeywordEvent(articleId, 999, Map.of(userId, "근로장학"));

        Article article = mock(Article.class);
        Board board = mock(Board.class);
        when(articleRepository.getById(articleId)).thenReturn(article);
        when(article.getId()).thenReturn(articleId);
        when(article.getTitle()).thenReturn("근로장학생 모집");
        when(article.getBoard()).thenReturn(board);
        when(board.getId()).thenReturn(boardId);
        when(notificationSubscribeRepository.findAllBySubscribeTypeAndDetailTypeIsNullWithUser(ARTICLE_KEYWORD))
            .thenReturn(List.of(subscribeA, subscribeB));
        when(userNotificationStatusRepository.findUserIdsByNotifiedArticleIdAndUserIdIn(eq(articleId), any()))
            .thenReturn(List.of());

        Notification notification = mock(Notification.class);
        when(notification.getUser()).thenReturn(subscriber);
        when(notificationFactory.generateKeywordNotification(any(), anyInt(), anyString(), anyString(), anyInt(), anyString(), any()))
            .thenReturn(notification);
        when(notificationService.pushNotificationsWithResult(any()))
            .thenReturn(List.of(new NotificationService.NotificationDeliveryResult(notification, true)));

        articleKeywordEventListener.onKeywordRequest(event);

        verify(notificationFactory, times(1)).generateKeywordNotification(
            eq(KEYWORD),
            eq(articleId),
            eq("근로장학"),
            eq("근로장학생 모집"),
            eq(boardId),
            contains("근로장학"),
            eq(subscriber)
        );
        verify(keywordService, times(1)).createNotifiedArticleStatus(userId, articleId);
        verify(notificationService).pushNotificationsWithResult(argThat(notifications ->
            notifications.size() == 1 && notifications.contains(notification)
        ));
    }

    @Test
    @DisplayName("게시글 작성자 본인에게는 키워드 알림을 보내지 않는다.")
    void onKeywordRequest_whenSubscriberIsAuthor_skipsNotification() {
        Integer articleId = 200;
        Integer userId = 2;
        User subscriber = UserFixture.id_설정_코인_유저(userId);
        subscriber.permitNotification("device-token");

        NotificationSubscribe subscribe = createKeywordSubscribe(subscriber);
        ArticleKeywordEvent event = new ArticleKeywordEvent(articleId, userId, Map.of(userId, "A"));

        Article article = mock(Article.class);
        Board board = mock(Board.class);
        when(articleRepository.getById(articleId)).thenReturn(article);
        when(article.getBoard()).thenReturn(board);
        when(notificationSubscribeRepository.findAllBySubscribeTypeAndDetailTypeIsNullWithUser(ARTICLE_KEYWORD))
            .thenReturn(List.of(subscribe));
        when(userNotificationStatusRepository.findUserIdsByNotifiedArticleIdAndUserIdIn(eq(articleId), any()))
            .thenReturn(List.of());
        when(notificationService.pushNotificationsWithResult(any())).thenReturn(List.of());

        articleKeywordEventListener.onKeywordRequest(event);

        verify(notificationFactory, never()).generateKeywordNotification(
            any(),
            anyInt(),
            anyString(),
            anyString(),
            anyInt(),
            anyString(),
            any()
        );
        verify(keywordService, never()).createNotifiedArticleStatus(anyInt(), anyInt());
        verify(notificationService).pushNotificationsWithResult(argThat(List::isEmpty));
    }

    @Test
    @DisplayName("이미 해당 게시글 알림을 받은 사용자는 다시 발송하지 않는다.")
    void onKeywordRequest_whenAlreadyNotified_skipsNotification() {
        Integer articleId = 300;
        Integer userId = 3;
        User subscriber = UserFixture.id_설정_코인_유저(userId);
        subscriber.permitNotification("device-token");

        NotificationSubscribe subscribe = createKeywordSubscribe(subscriber);
        ArticleKeywordEvent event = new ArticleKeywordEvent(articleId, 999, Map.of(userId, "C"));

        Article article = mock(Article.class);
        Board board = mock(Board.class);
        when(articleRepository.getById(articleId)).thenReturn(article);
        when(article.getBoard()).thenReturn(board);
        when(notificationSubscribeRepository.findAllBySubscribeTypeAndDetailTypeIsNullWithUser(ARTICLE_KEYWORD))
            .thenReturn(List.of(subscribe));
        when(userNotificationStatusRepository.findUserIdsByNotifiedArticleIdAndUserIdIn(eq(articleId), any()))
            .thenReturn(List.of(userId));
        when(notificationService.pushNotificationsWithResult(any())).thenReturn(List.of());

        articleKeywordEventListener.onKeywordRequest(event);

        verify(notificationFactory, never()).generateKeywordNotification(
            any(),
            anyInt(),
            anyString(),
            anyString(),
            anyInt(),
            anyString(),
            any()
        );
        verify(keywordService, never()).createNotifiedArticleStatus(anyInt(), anyInt());
        verify(notificationService).pushNotificationsWithResult(argThat(List::isEmpty));
    }

    @Test
    @DisplayName("알림 전송 실패 시 발송 이력을 저장하지 않는다.")
    void onKeywordRequest_whenDeliveryFails_doesNotRecordNotifiedStatus() {
        Integer articleId = 400;
        Integer boardId = 15;
        Integer userId = 4;
        User subscriber = UserFixture.id_설정_코인_유저(userId);
        subscriber.permitNotification("device-token");

        NotificationSubscribe subscribe = createKeywordSubscribe(subscriber);
        ArticleKeywordEvent event = new ArticleKeywordEvent(articleId, 999, Map.of(userId, "근로장학"));

        Article article = mock(Article.class);
        Board board = mock(Board.class);
        when(articleRepository.getById(articleId)).thenReturn(article);
        when(article.getId()).thenReturn(articleId);
        when(article.getTitle()).thenReturn("근로장학생 모집");
        when(article.getBoard()).thenReturn(board);
        when(board.getId()).thenReturn(boardId);
        when(notificationSubscribeRepository.findAllBySubscribeTypeAndDetailTypeIsNullWithUser(ARTICLE_KEYWORD))
            .thenReturn(List.of(subscribe));
        when(userNotificationStatusRepository.findUserIdsByNotifiedArticleIdAndUserIdIn(eq(articleId), any()))
            .thenReturn(List.of());

        Notification notification = mock(Notification.class);
        when(notificationFactory.generateKeywordNotification(any(), anyInt(), anyString(), anyString(), anyInt(), anyString(), any()))
            .thenReturn(notification);
        when(notificationService.pushNotificationsWithResult(any()))
            .thenReturn(List.of(new NotificationService.NotificationDeliveryResult(notification, false)));

        articleKeywordEventListener.onKeywordRequest(event);

        verify(notificationFactory, times(1)).generateKeywordNotification(
            eq(KEYWORD),
            eq(articleId),
            eq("근로장학"),
            eq("근로장학생 모집"),
            eq(boardId),
            contains("근로장학"),
            eq(subscriber)
        );
        verify(notificationService).pushNotificationsWithResult(argThat(notifications ->
            notifications.size() == 1 && notifications.contains(notification)
        ));
        verify(keywordService, never()).createNotifiedArticleStatus(anyInt(), anyInt());
    }

    @Test
    @DisplayName("기발송 사용자 조회는 매칭된 사용자 ID만 대상으로 수행한다.")
    void onKeywordRequest_queriesNotifiedStatusOnlyForMatchedUsers() {
        Integer articleId = 500;
        Integer boardId = 16;
        Integer matchedUserId = 5;
        Integer unmatchedUserId = 6;

        User matchedUser = UserFixture.id_설정_코인_유저(matchedUserId);
        matchedUser.permitNotification("matched-device-token");
        User unmatchedUser = UserFixture.id_설정_코인_유저(unmatchedUserId);
        unmatchedUser.permitNotification("unmatched-device-token");

        NotificationSubscribe matchedSubscribe = createKeywordSubscribe(matchedUser);
        NotificationSubscribe unmatchedSubscribe = createKeywordSubscribe(unmatchedUser);
        ArticleKeywordEvent event = new ArticleKeywordEvent(articleId, 999, Map.of(matchedUserId, "근로장학"));

        Article article = mock(Article.class);
        Board board = mock(Board.class);
        when(articleRepository.getById(articleId)).thenReturn(article);
        when(article.getId()).thenReturn(articleId);
        when(article.getTitle()).thenReturn("근로장학생 모집");
        when(article.getBoard()).thenReturn(board);
        when(board.getId()).thenReturn(boardId);
        when(notificationSubscribeRepository.findAllBySubscribeTypeAndDetailTypeIsNullWithUser(ARTICLE_KEYWORD))
            .thenReturn(List.of(matchedSubscribe, unmatchedSubscribe));
        when(userNotificationStatusRepository.findUserIdsByNotifiedArticleIdAndUserIdIn(articleId, Set.of(matchedUserId)))
            .thenReturn(List.of());

        Notification notification = mock(Notification.class);
        when(notificationFactory.generateKeywordNotification(any(), anyInt(), anyString(), anyString(), anyInt(), anyString(), any()))
            .thenReturn(notification);
        when(notificationService.pushNotificationsWithResult(any())).thenReturn(List.of());

        articleKeywordEventListener.onKeywordRequest(event);

        verify(userNotificationStatusRepository)
            .findUserIdsByNotifiedArticleIdAndUserIdIn(articleId, Set.of(matchedUserId));
    }

    private NotificationSubscribe createKeywordSubscribe(User user) {
        return NotificationSubscribe.builder()
            .subscribeType(ARTICLE_KEYWORD)
            .user(user)
            .build();
    }
}
