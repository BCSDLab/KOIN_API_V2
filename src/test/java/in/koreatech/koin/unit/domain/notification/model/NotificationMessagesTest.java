package in.koreatech.koin.unit.domain.notification.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import in.koreatech.koin.domain.notification.service.CoopNotificationService;
import in.koreatech.koin.domain.notification.service.NotificationService;
import in.koreatech.koin.domain.notification.model.Notification;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;
import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitmentApplication;
import in.koreatech.koin.domain.language.model.SupportedLanguage;

import in.koreatech.koin.common.model.MobileAppPath;
import in.koreatech.koin.domain.callvan.dto.CallvanNotificationResponse;
import in.koreatech.koin.domain.callvan.model.CallvanChatRoom;
import in.koreatech.koin.domain.callvan.model.CallvanNotification;
import in.koreatech.koin.domain.callvan.model.CallvanParticipant;
import in.koreatech.koin.domain.callvan.model.CallvanPost;
import in.koreatech.koin.domain.callvan.model.CallvanPushNotificationFactory;
import in.koreatech.koin.domain.callvan.model.enums.CallvanLocation;
import in.koreatech.koin.domain.callvan.model.enums.CallvanMessageType;
import in.koreatech.koin.domain.callvan.model.enums.CallvanNotificationType;
import in.koreatech.koin.domain.callvan.repository.CallvanNotificationRepository;
import in.koreatech.koin.domain.callvan.repository.CallvanPostRepository;
import in.koreatech.koin.domain.callvan.service.CallvanNotificationService;
import in.koreatech.koin.domain.user.repository.UserRepository;
import in.koreatech.koin.domain.notification.i18n.NotificationMessages;
import in.koreatech.koin.domain.notification.model.NotificationFactory;
import in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentNotificationType;
import in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentNotificationTargetType;
import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitment;
import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitmentNotification;
import in.koreatech.koin.domain.teamrecruitment.dto.TeamRecruitmentNotificationResponse;
import in.koreatech.koin.domain.user.model.User;

class NotificationMessagesTest {
    @Test
    void 미설정_언어와_미등록_문구는_원문으로_대체한다() {
        User user = mock(User.class);
        String template = "%s님의 메시지";
        assertThat(NotificationMessages.format(null, template, "닉네임")).isEqualTo("닉네임님의 메시지");
        assertThat(NotificationMessages.format(user, template, "닉네임")).isEqualTo("닉네임님의 메시지");
        when(user.getPreferredLanguage()).thenReturn("unsupported");
        assertThat(NotificationMessages.format(user, template, "닉네임")).isEqualTo("닉네임님의 메시지");

        when(user.getPreferredLanguage()).thenReturn("en-us");
        assertThat(NotificationMessages.format(user, "미등록 %s 안내", "50% 행사"))
            .isEqualTo("미등록 50% 행사 안내");
        assertThat(NotificationMessages.translateKnown(user, "미등록 안내")).isEqualTo("미등록 안내");
        assertThat(NotificationMessages.translateKnown(user, template)).isEqualTo(template);
        assertThat(NotificationMessages.translateKnown(user, null)).isNull();
        NotificationFactory factory = new NotificationFactory();
        assertThat(factory.generateSoldOutNotification(MobileAppPath.DINING, 1, "A코너", user).getTitle())
            .isEqualTo("Corner A is sold out 😢");
        assertThat(factory.generateSoldOutNotification(MobileAppPath.DINING, 1, "임시코너", user).getTitle())
            .isEqualTo("임시코너 is sold out 😢");
        for (SupportedLanguage language : SupportedLanguage.values()) {
            when(user.getPreferredLanguage()).thenReturn(language.getCode());
            String message = factory.generateDiningReportApprovedNotification(1, user).getMessage();
            assertThat(message).isNotBlank();
            if (language != SupportedLanguage.KOREAN) {
                assertThat(message).isNotEqualTo("품절 제보가 승인되었습니다.");
            }
        }
        when(user.getPreferredLanguage()).thenReturn(null);
        assertThat(factory.generateSoldOutNotification(MobileAppPath.DINING, 1, "A코너", user).getTitle())
            .isEqualTo("A코너가 품절됐어요 😢");
        assertThat(factory.generateDiningReportApprovedNotification(1, user).getMessage())
            .isEqualTo("품절 제보가 승인되었습니다.");
        when(user.getPreferredLanguage()).thenReturn("en-US");
        when(user.getDeviceToken()).thenReturn("test-token");
        User noToken = mock(User.class);
        UserRepository users = mock(UserRepository.class);
        when(users.findAllByIdIn(List.of(1, 2))).thenReturn(List.of(user, noToken));
        NotificationService notifications = mock(NotificationService.class);
        // 이 경로는 구독 목록을 읽지 않고, 발송 시 조회한 계정의 언어와 토큰을 사용한다.
        CoopNotificationService service = new CoopNotificationService(null, factory, notifications,
            null, null, null, null, users);
        service.sendDiningReportApprovedNotifications(7, List.of(1, 2));
        ArgumentCaptor<List<Notification>> sent = ArgumentCaptor.forClass(List.class);
        verify(notifications).pushNotifications(sent.capture());
        assertThat(sent.getValue()).singleElement().satisfies(notification -> {
            assertThat(notification.getUser()).isSameAs(user);
            assertThat(notification.getMessage()).isEqualTo("Your sold-out report has been approved.");
        });
        assertThat(NotificationMessages.format(user, "%s에 모집 정보가 새로 업데이트되었어요.\n행사 내용 둘러보기", "KOIN"))
            .isEqualTo("KOIN has new recruitment information.\nView event details");
    }

    @Test
    void 시스템문구와_같은_사용자_제목과_쪽지도_원문을_보존한다() {
        User user = mock(User.class);
        when(user.getPreferredLanguage()).thenReturn("en-US");
        String title = "지원하신 X 모집이 마감되어";
        assertThat(NotificationMessages.teamMessage(user, TeamRecruitmentNotificationType.APPLICATION_REJECTED,
            title, title + " 지원이 거절되었어요.", null))
            .isEqualTo("Your application to " + title + " was declined.");
        TeamRecruitment recruitment = mock(TeamRecruitment.class);
        when(recruitment.getTitle()).thenReturn(title);
        TeamRecruitmentNotification notification = TeamRecruitmentNotification.builder()
            .recipient(user).recruitment(recruitment).type(TeamRecruitmentNotificationType.APPLICATION_REJECTED)
            .targetType(TeamRecruitmentNotificationTargetType.MY_APPLICATIONS)
            .messagePreview(title + " 지원이 거절되었어요.").build();
        when(recruitment.getTitle()).thenReturn("변경된 제목");
        assertThat(TeamRecruitmentNotificationResponse.from(notification).messagePreview())
            .isEqualTo("Your application to " + title + " was declined.");
        assertThat(NotificationMessages.teamMessage(user, TeamRecruitmentNotificationType.NEW_APPLICATION,
            null, "A에 새로운 지원자가 있어요.", null))
            .isEqualTo("A has a new applicant.");
        assertThat(NotificationMessages.teamMessage(user, TeamRecruitmentNotificationType.APPLICATION_REJECTED,
            null, title + " 지원이 거절되었어요.", null))
            .isEqualTo(title + " 지원이 거절되었어요.");
        // 구형 행에는 snapshot이 없다. 현재 제목 X를 과거 거절 사유의 증거로 사용하지 않는다.
        ReflectionTestUtils.setField(notification, "recruitmentTitle", null);
        when(recruitment.getTitle()).thenReturn("X");
        TeamRecruitmentApplication application = mock(TeamRecruitmentApplication.class);
        ReflectionTestUtils.setField(notification, "application", application);
        when(application.getDecisionReason()).thenReturn("APPLICATION_REJECTED");
        assertThat(TeamRecruitmentNotificationResponse.from(notification).messagePreview())
            .isEqualTo("Your application to " + title + " was declined.");
        when(application.getDecisionReason()).thenReturn("RECRUITMENT_CLOSED");
        assertThat(TeamRecruitmentNotificationResponse.from(notification).messagePreview())
            .isEqualTo("Your application to X was declined because recruitment closed.");
        when(application.getDecisionReason()).thenReturn(null);
        assertThat(TeamRecruitmentNotificationResponse.from(notification).messagePreview())
            .isEqualTo(title + " 지원이 거절되었어요.");
        var chat = new NotificationFactory().generateChatMessageNotification(MobileAppPath.SHOP,
            1, 2, "닉네임", "주문이 접수되었어요", user);
        assertThat(chat.getTitle()).isEqualTo("Message from 닉네임");
        assertThat(chat.getMessage()).isEqualTo("주문이 접수되었어요");

        NotificationFactory factory = new NotificationFactory();
        var shopEvent = factory.generateShopEventCreateNotification(MobileAppPath.SHOP,
            1, null, "멕시카나", "주문이 접수되었어요", user);
        assertThat(shopEvent.getTitle()).isEqualTo("Mexicana has added an event 🎉");
        assertThat(shopEvent.getMessage()).isEqualTo("주문이 접수되었어요");
        assertThat(factory.generateReviewPromptNotification(MobileAppPath.SHOP, 1,
            "멕시카나", ",맛있게 드셨나요?", "가게에 리뷰를 남겨보세요!", user).getTitle())
            .isEqualTo("Mexicana, did you enjoy your meal?");
        assertThat(factory.generateOrderNotification(MobileAppPath.SHOP, 1,
            "멕시카나", "PACKAGED", false, null, user).getMessage())
            .isEqualTo("Please pick up your order at Mexicana.");
        assertThat(factory.generateShopEventCreateNotification(MobileAppPath.SHOP,
            1, null, "미등록 상점", "행사", user).getTitle())
            .isEqualTo("미등록 상점 has added an event 🎉");
        when(user.getPreferredLanguage()).thenReturn(null);
        assertThat(factory.generateShopEventCreateNotification(MobileAppPath.SHOP,
            1, null, "멕시카나", "행사", user).getTitle())
            .isEqualTo("멕시카나의 이벤트가 추가되었어요 🎉");
    }

    @Test
    void 콜밴_시스템_알림은_발송과_조회_시점_언어를_사용하고_사용자_입력은_보존한다() {
        User recipient = mock(User.class);
        when(recipient.getId()).thenReturn(2);
        when(recipient.getPreferredLanguage()).thenReturn("en-US");
        CallvanPost post = mock(CallvanPost.class);
        when(post.getParticipants()).thenReturn(List.of(CallvanParticipant.builder().member(recipient).build()));
        when(post.getChatRoom()).thenReturn(mock(CallvanChatRoom.class));
        when(post.getDepartureType()).thenReturn(CallvanLocation.FRONT_GATE);
        when(post.getArrivalType()).thenReturn(CallvanLocation.STATION);
        CallvanPostRepository posts = mock(CallvanPostRepository.class);
        when(posts.getById(1)).thenReturn(post);
        CallvanNotificationRepository notifications = mock(CallvanNotificationRepository.class);
        List<CallvanNotification> saved = new ArrayList<>();
        when(notifications.saveAllAndFlush(anyList())).thenAnswer(invocation -> {
            List<CallvanNotification> batch = invocation.getArgument(0);
            saved.addAll(batch);
            return batch;
        });
        var service = new CallvanNotificationService(posts, notifications, mock(UserRepository.class),
            mock(ApplicationEventPublisher.class));
        String original = "닉네임님이 사진을 보냈습니다.";
        service.notifyNewMessageReceived(1, 1, "닉네임", "photo.jpg", CallvanMessageType.IMAGE);
        service.notifyNewMessageReceived(1, 1, "닉네임", original, CallvanMessageType.TEXT);

        assertThat(saved).extracting(CallvanNotification::getMessageType)
            .containsExactly(CallvanMessageType.IMAGE, CallvanMessageType.TEXT);
        assertThat(saved).extracting(CallvanNotification::getMessagePreview).containsOnly(original);
        var factory = new CallvanPushNotificationFactory();
        assertThat(factory.from(saved.get(0)).message()).isEqualTo("닉네임 sent a photo.");
        when(recipient.getPreferredLanguage()).thenReturn("ja-JP");
        assertThat(factory.from(saved.get(0)).message()).isEqualTo("닉네임さんが写真を送信しました。");
        assertThat(CallvanNotificationResponse.from(saved.get(0)).messagePreview())
            .isEqualTo("닉네임さんが写真を送信しました。");
        assertThat(factory.from(saved.get(1)).message()).isEqualTo(original);
        assertThat(CallvanPushNotificationFactory.localizedPreview(CallvanNotification.builder()
            .recipient(recipient).notificationType(CallvanNotificationType.NEW_MESSAGE)
            .senderNickname("닉네임").messagePreview(original).build())).isEqualTo(original);

        when(recipient.getPreferredLanguage()).thenReturn("en-US");
        CallvanNotification departure = CallvanNotification.builder()
            .recipient(recipient).notificationType(CallvanNotificationType.DEPARTURE_UPCOMING)
            .departureType(CallvanLocation.TENNIS_COURT).departureCustomName("테니스장")
            .arrivalType(CallvanLocation.STATION).arrivalCustomName("천안역").build();
        String expected = "Callvan from Tennis Court to Cheonan Station departs in 30 minutes";
        assertThat(factory.from(departure).message()).isEqualTo(expected);
        CallvanNotificationResponse response = CallvanNotificationResponse.from(departure);
        assertThat(response.messagePreview()).isEqualTo(expected);
        assertThat(response.departure()).isEqualTo("Tennis Court");
        assertThat(response.arrival()).isEqualTo("Cheonan Station");
        assertThat(CallvanPushNotificationFactory.localizedLocationName("천안터미널", CallvanLocation.TERMINAL, recipient))
            .isEqualTo("Bus Terminal");
        assertThat(CallvanPushNotificationFactory.localizedPreview(CallvanNotification.builder()
            .recipient(recipient).notificationType(CallvanNotificationType.DEPARTURE_UPCOMING)
            .departureType(CallvanLocation.CUSTOM).departureCustomName("테니스장")
            .arrivalType(CallvanLocation.FRONT_GATE).build()))
            .isEqualTo("Callvan from 테니스장 to 정문 departs in 30 minutes");
    }
}
