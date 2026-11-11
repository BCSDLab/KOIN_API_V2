package in.koreatech.koin.unit.domain.callvan.service;

import static in.koreatech.koin.domain.notification.model.NotificationSubscribeType.CALLVAN;
import static in.koreatech.koin.domain.callvan.model.enums.CallvanNotificationType.NEW_MESSAGE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import in.koreatech.koin.common.model.MobileAppPath;
import in.koreatech.koin.domain.callvan.model.CallvanChatRoom;
import in.koreatech.koin.domain.callvan.model.CallvanNotification;
import in.koreatech.koin.domain.callvan.model.CallvanPost;
import in.koreatech.koin.domain.callvan.model.CallvanPushNotification;
import in.koreatech.koin.domain.callvan.model.CallvanPushNotificationFactory;
import in.koreatech.koin.domain.callvan.model.enums.CallvanLocation;
import in.koreatech.koin.domain.callvan.model.enums.CallvanNotificationType;
import in.koreatech.koin.domain.callvan.repository.CallvanNotificationRepository;
import in.koreatech.koin.domain.callvan.repository.CallvanParticipantRepository;
import in.koreatech.koin.domain.chat.service.ChatPresenceService;
import in.koreatech.koin.domain.callvan.service.CallvanPushNotificationService;
import in.koreatech.koin.domain.notification.repository.NotificationSubscribeRepository;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.infrastructure.fcm.FcmClient;
import in.koreatech.koin.infrastructure.fcm.FcmSendRequest;
import in.koreatech.koin.unit.fixture.UserFixture;

@ExtendWith(MockitoExtension.class)
class CallvanPushNotificationServiceTest {

    @Mock
    private CallvanNotificationRepository notificationRepository;
    @Mock
    private NotificationSubscribeRepository subscribeRepository;
    @Spy
    private CallvanPushNotificationFactory factory = new CallvanPushNotificationFactory();
    @Mock
    private FcmClient fcmClient;
    @Mock private CallvanParticipantRepository participantRepository;
    @Mock private ChatPresenceService presence;
    private CallvanPushNotificationService service;

    @BeforeEach
    void setUp() {
        service = new CallvanPushNotificationService(notificationRepository, subscribeRepository, factory,
            fcmClient);
    }

    @Test
    void 새_메시지는_기존_필드와_공통_채팅_정보를_한번만_전송한다() {
        CallvanNotification notification = notification(NEW_MESSAGE, "device-token");
        eligible(notification);

        service.pushNotifications(List.of(530));

        ArgumentCaptor<FcmSendRequest> sent = ArgumentCaptor.forClass(FcmSendRequest.class);
        verify(fcmClient).sendMessage(sent.capture());
        FcmSendRequest request = sent.getValue();
        assertThat(request.targetDeviceToken()).isEqualTo("device-token");
        assertThat(request.title()).isEqualTo("콜벤팟 발신자님의 메시지");
        assertThat(request.content()).isEqualTo("기존 메시지");
        assertThat(request.imageUrl()).isNull();
        assertThat(request.path()).isEqualTo(MobileAppPath.CALLVAN_CHAT);
        assertThat(request.schemeUri()).isEqualTo("callvan-chat?postId=30&chatRoomId=12");
        assertThat(request.type()).isEqualTo("new_message");
        assertThat(request.notificationId()).isNull();
        assertThat(request.roomId()).isEqualTo("callvan-30-12");
        verifyNoMoreInteractions(fcmClient);
    }

    @ParameterizedTest
    @EnumSource(value = CallvanNotificationType.class, names = "PARTICIPANT_JOINED")
    void 채팅_외_알림은_기존_필드를_유지하고_공통_방이_없다(CallvanNotificationType type) {
        CallvanNotification notification = notification(type, "device-token");
        eligible(notification);
        CallvanPushNotification expected = new CallvanPushNotificationFactory().from(notification);

        service.pushNotifications(List.of(530));

        ArgumentCaptor<FcmSendRequest> sent = ArgumentCaptor.forClass(FcmSendRequest.class);
        verify(fcmClient).sendMessage(sent.capture());
        assertThat(sent.getValue()).isEqualTo(FcmSendRequest.of(
            "device-token", expected.title(), expected.message(), expected.imageUrl(),
            expected.mobileAppPath(), expected.schemeUri(), expected.type()));
        assertThat(sent.getValue().roomId()).isNull();
        verifyNoInteractions(participantRepository, presence);
        verifyNoMoreInteractions(fcmClient);
    }

    @Test
    void 구독하지_않은_수신자에게는_전송하지_않는다() {
        when(notificationRepository.findAllByIdInWithRelations(List.of(530)))
            .thenReturn(List.of(notification(NEW_MESSAGE, "device-token")));

        service.pushNotifications(List.of(530));

        verifyNoInteractions(factory, fcmClient);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = " ")
    void 토큰이_없으면_전송하지_않는다(String token) {
        when(notificationRepository.findAllByIdInWithRelations(List.of(530)))
            .thenReturn(List.of(notification(NEW_MESSAGE, token)));

        service.pushNotifications(List.of(530));

        verifyNoInteractions(subscribeRepository, factory, fcmClient);
    }

    @Test
    void unreadPushDoesNotConsultRoomPresence() {
        eligible(notification(NEW_MESSAGE, "device-token"));
        service.pushNotifications(List.of(530));
        verify(fcmClient).sendMessage(any(FcmSendRequest.class));
        verifyNoInteractions(presence);
    }

    @Test
    void queuedPushDoesNotRecheckReadMembershipOrMessageState() {
        eligible(notification(NEW_MESSAGE, "device-token"));
        service.pushNotifications(List.of(530));
        verify(fcmClient).sendMessage(any(FcmSendRequest.class));
        verifyNoInteractions(participantRepository, presence);
    }

    @Test
    void savedMessageEventCreatesInboxBeforePush() {
        var postRepository = org.mockito.Mockito.mock(
            in.koreatech.koin.domain.callvan.repository.CallvanPostRepository.class);
        var userRepository = org.mockito.Mockito.mock(
            in.koreatech.koin.domain.user.repository.UserRepository.class);
        var publisher = org.mockito.Mockito.mock(org.springframework.context.ApplicationEventPublisher.class);
        var notifications = new in.koreatech.koin.domain.callvan.service.CallvanNotificationService(
            postRepository, notificationRepository, userRepository, publisher);
        var listener = new in.koreatech.koin.domain.callvan.service.CallvanNotificationEventListener(notifications);
        CallvanNotification saved = notification(NEW_MESSAGE, "device-token");
        ReflectionTestUtils.setField(saved, "id", 530);
        var post = saved.getPost();
        post.getParticipants().add(in.koreatech.koin.domain.callvan.model.CallvanParticipant.builder()
            .post(post).member(saved.getRecipient()).build());
        when(postRepository.getById(30)).thenReturn(post);
        when(notificationRepository.saveAllAndFlush(org.mockito.ArgumentMatchers.anyList()))
            .thenReturn(List.of(saved));

        listener.onNewMessage(new in.koreatech.koin.domain.callvan.event.CallvanNewMessageEvent(
            30, "발신자", 1, "기존 메시지",
            in.koreatech.koin.domain.callvan.model.enums.CallvanMessageType.TEXT));

        ArgumentCaptor<in.koreatech.koin.domain.callvan.event.CallvanPushNotificationEvent> event =
            ArgumentCaptor.forClass(in.koreatech.koin.domain.callvan.event.CallvanPushNotificationEvent.class);
        var order = org.mockito.Mockito.inOrder(notificationRepository, publisher);
        order.verify(notificationRepository).saveAllAndFlush(org.mockito.ArgumentMatchers.anyList());
        order.verify(publisher).publishEvent(event.capture());
        assertThat(event.getValue().notificationIds()).containsExactly(530);

        eligible(saved);
        new in.koreatech.koin.domain.callvan.service.CallvanPushNotificationEventListener(service)
            .onCallvanPushRequested(event.getValue());
        verify(fcmClient).sendMessage(any(FcmSendRequest.class));
        verifyNoInteractions(presence);
        verifyNoMoreInteractions(publisher);
    }

    private void eligible(CallvanNotification notification) {
        when(notificationRepository.findAllByIdInWithRelations(List.of(530))).thenReturn(List.of(notification));
        when(subscribeRepository.existsByUserIdAndSubscribeTypeAndDetailTypeIsNull(2, CALLVAN))
            .thenReturn(true);
    }

    private CallvanNotification notification(CallvanNotificationType type, String token) {
        User recipient = UserFixture.id_설정_코인_유저(2);
        recipient.permitNotification(token);
        CallvanPost post = CallvanPost.builder().build();
        ReflectionTestUtils.setField(post, "id", 30);
        CallvanChatRoom room = CallvanChatRoom.builder().post(post).roomName("콜벤팟").build();
        ReflectionTestUtils.setField(room, "id", 12);
        post.updateChatRoom(room);
        return CallvanNotification.builder().recipient(recipient).notificationType(type)
            .post(post).chatRoom(room).senderNickname("발신자").messagePreview("기존 메시지")
            .joinedMemberNickname("참여자").departureType(CallvanLocation.values()[0])
            .arrivalType(CallvanLocation.values()[0]).build();
    }
}
