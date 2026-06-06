package in.koreatech.koin.unit.domain.notification.service;

import static in.koreatech.koin.common.model.MobileAppPath.KEYWORD;
import static in.koreatech.koin.domain.notification.model.NotificationSubscribeType.ARTICLE_KEYWORD;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import in.koreatech.koin.common.event.KoreatechArticleKeywordEvent;
import in.koreatech.koin.domain.notification.model.Notification;
import in.koreatech.koin.domain.notification.model.NotificationFactory;
import in.koreatech.koin.domain.notification.model.NotificationSubscribe;
import in.koreatech.koin.domain.notification.repository.NotificationSubscribeRepository;
import in.koreatech.koin.domain.notification.service.KeywordNotificationService;
import in.koreatech.koin.domain.notification.service.NotificationService;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.unit.fixture.KeywordFixture;
import in.koreatech.koin.unit.fixture.NotificationFixture;
import in.koreatech.koin.unit.fixture.UserFixture;

@ExtendWith(MockitoExtension.class)
public class KeywordNotificationServiceTest {

    @InjectMocks
    private KeywordNotificationService keywordNotificationService;

    @Mock
    private NotificationFactory notificationFactory;

    @Mock
    private NotificationSubscribeRepository notificationSubscribeRepository;

    @Mock
    private NotificationService notificationService;

    @Test
    void 매칭된_사용자가_없으면_알림을_처리하지_않는다() {
        KoreatechArticleKeywordEvent event = KeywordFixture.공지_키워드_이벤트(
            1,
            4,
            "수강신청 안내",
            Map.of()
        );

        keywordNotificationService.notifyArticleKeyword(event);

        verifyNoInteractions(notificationSubscribeRepository, notificationFactory, notificationService);
    }

    @Test
    void 매칭된_사용자의_공지_키워드_구독을_조회한다() {
        KoreatechArticleKeywordEvent event = KeywordFixture.공지_키워드_이벤트(
            1,
            4,
            "수강신청 안내",
            Map.of(
                1, "수강신청",
                2, "장학금"
            )
        );
        when(notificationSubscribeRepository.findArticleKeywordSubscribesByUserIdIn(
            eq(ARTICLE_KEYWORD),
            anyList()
        )).thenReturn(List.of());

        keywordNotificationService.notifyArticleKeyword(event);

        ArgumentCaptor<List<Integer>> userIdsCaptor = ArgumentCaptor.forClass(List.class);
        verify(notificationSubscribeRepository)
            .findArticleKeywordSubscribesByUserIdIn(eq(ARTICLE_KEYWORD), userIdsCaptor.capture());
        assertThat(userIdsCaptor.getValue()).containsExactlyInAnyOrder(1, 2);
    }

    @Test
    void 구독자별로_키워드_알림을_생성하고_푸시한다() {
        User firstUser = UserFixture.id_설정_코인_유저(1);
        User secondUser = UserFixture.id_설정_코인_유저(2);
        NotificationSubscribe firstSubscribe = NotificationFixture.공지_키워드_구독(firstUser);
        NotificationSubscribe secondSubscribe = NotificationFixture.공지_키워드_구독(secondUser);
        KoreatechArticleKeywordEvent event = KeywordFixture.공지_키워드_이벤트(
            100,
            4,
            "수강신청 안내",
            Map.of(
                firstUser.getId(), "수강신청",
                secondUser.getId(), "신청"
            )
        );
        Notification firstNotification = NotificationFixture.키워드_알림(firstUser);
        Notification secondNotification = NotificationFixture.키워드_알림(secondUser);
        when(notificationSubscribeRepository.findArticleKeywordSubscribesByUserIdIn(
            eq(ARTICLE_KEYWORD),
            anyList()
        )).thenReturn(List.of(firstSubscribe, secondSubscribe));
        when(notificationFactory.generateKeywordNotification(
            KEYWORD,
            event.articleId(),
            "수강신청",
            event.articleTitle(),
            event.boardId(),
            firstUser
        )).thenReturn(firstNotification);
        when(notificationFactory.generateKeywordNotification(
            KEYWORD,
            event.articleId(),
            "신청",
            event.articleTitle(),
            event.boardId(),
            secondUser
        )).thenReturn(secondNotification);

        keywordNotificationService.notifyArticleKeyword(event);

        ArgumentCaptor<List<Notification>> notificationsCaptor = ArgumentCaptor.forClass(List.class);
        verify(notificationService).pushNotifications(notificationsCaptor.capture());
        assertThat(notificationsCaptor.getValue()).containsExactly(firstNotification, secondNotification);
    }

    @Test
    void 매칭된_사용자라도_구독자가_아니면_알림을_생성하지_않는다() {
        User subscribedUser = UserFixture.id_설정_코인_유저(1);
        KoreatechArticleKeywordEvent event = KeywordFixture.공지_키워드_이벤트(
            100,
            4,
            "수강신청 안내",
            Map.of(
                subscribedUser.getId(), "수강신청",
                2, "장학금"
            )
        );
        NotificationSubscribe subscribe = NotificationFixture.공지_키워드_구독(subscribedUser);
        Notification notification = NotificationFixture.키워드_알림(subscribedUser);
        when(notificationSubscribeRepository.findArticleKeywordSubscribesByUserIdIn(
            eq(ARTICLE_KEYWORD),
            anyList()
        )).thenReturn(List.of(subscribe));
        when(notificationFactory.generateKeywordNotification(
            KEYWORD,
            event.articleId(),
            "수강신청",
            event.articleTitle(),
            event.boardId(),
            subscribedUser
        )).thenReturn(notification);

        keywordNotificationService.notifyArticleKeyword(event);

        verify(notificationFactory).generateKeywordNotification(
            KEYWORD,
            event.articleId(),
            "수강신청",
            event.articleTitle(),
            event.boardId(),
            subscribedUser
        );
        verify(notificationFactory, never()).generateKeywordNotification(
            eq(KEYWORD),
            eq(event.articleId()),
            eq("장학금"),
            eq(event.articleTitle()),
            eq(event.boardId()),
            any(User.class)
        );
        ArgumentCaptor<List<Notification>> notificationsCaptor = ArgumentCaptor.forClass(List.class);
        verify(notificationService).pushNotifications(notificationsCaptor.capture());
        assertThat(notificationsCaptor.getValue()).containsExactly(notification);
    }

}
