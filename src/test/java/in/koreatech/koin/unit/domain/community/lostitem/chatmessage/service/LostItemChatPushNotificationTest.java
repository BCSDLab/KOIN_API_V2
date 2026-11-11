package in.koreatech.koin.unit.domain.community.lostitem.chatmessage.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.test.util.ReflectionTestUtils;

import in.koreatech.koin.common.model.MobileAppPath;
import in.koreatech.koin.domain.community.lostitem.chatmessage.dto.ChatMessage;
import in.koreatech.koin.domain.community.lostitem.chatmessage.event.MessageSendEvent;
import in.koreatech.koin.domain.community.lostitem.chatmessage.event.PollingMessageSendEvent;
import in.koreatech.koin.domain.community.lostitem.chatmessage.service.ChatMessagePollingNotificationSendService;
import in.koreatech.koin.domain.community.lostitem.chatmessage.service.ChatMessageSendService;
import in.koreatech.koin.domain.community.lostitem.chatroom.model.LostItemChatRoomInfoEntity;
import in.koreatech.koin.domain.community.lostitem.chatroom.repository.LostItemChatRoomInfoRepository;
import in.koreatech.koin.domain.community.lostitem.chatroom.service.LostItemChatRoomInfoService;
import in.koreatech.koin.domain.notification.model.NotificationFactory;
import in.koreatech.koin.domain.notification.model.NotificationSubscribeType;
import in.koreatech.koin.domain.notification.repository.NotificationJdbcRepository;
import in.koreatech.koin.domain.notification.repository.NotificationSubscribeRepository;
import in.koreatech.koin.domain.notification.service.NotificationService;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.model.UserType;
import in.koreatech.koin.domain.user.repository.UserRepository;
import in.koreatech.koin.infrastructure.fcm.FcmClient;
import in.koreatech.koin.infrastructure.fcm.FcmSendRequest;
import in.koreatech.koin.infrastructure.fcm.FcmSendResponse;
import in.koreatech.koin.socket.session.service.WebSocketUserSessionService;

class LostItemChatPushNotificationTest {

    @ParameterizedTest
    @MethodSource("senderNames")
    void 채팅_푸시는_공통_방_ID와_발신자_표시이름과_기존_이동_정보를_보존한다(
        boolean polling, String nickname, String anonymousNickname, String expectedTitle
    ) {
        UserRepository users = mock(UserRepository.class);
        NotificationSubscribeRepository subscriptions = mock(NotificationSubscribeRepository.class);
        FcmClient fcm = mock(FcmClient.class);
        NotificationJdbcRepository history = mock(NotificationJdbcRepository.class);
        NotificationService notifications = new NotificationService(fcm, users, history, subscriptions);
        NotificationFactory factory = new NotificationFactory();
        User sender = User.builder().id(7).nickname(nickname).anonymousNickname(anonymousNickname)
            .userType(UserType.STUDENT).isAuthed(true).isDeleted(false).build();
        // 생성자가 자동 생성하는 값 대신, 익명 이름도 비어 있는 기존 저장 행을 재현한다.
        ReflectionTestUtils.setField(sender, "anonymousNickname", anonymousNickname);
        User receiver = User.builder().id(8).deviceToken("test-device")
            .userType(UserType.STUDENT).isAuthed(true).isDeleted(false).build();
        when(users.getById(7)).thenReturn(sender);
        when(users.getById(8)).thenReturn(receiver);
        when(subscriptions.existsByUserIdAndSubscribeTypeAndDetailTypeIsNull(8, NotificationSubscribeType.LOST_ITEM_CHAT))
            .thenReturn(true);
        when(fcm.sendMessages(anyList())).thenReturn(List.of(FcmSendResponse.succeeded()));
        ChatMessage message = new ChatMessage(7, "클라이언트 이름", "저장된 메시지", LocalDateTime.now(), false);

        if (polling) {
            new ChatMessagePollingNotificationSendService(notifications, factory, users, subscriptions)
                .handle(new PollingMessageSendEvent(10, 2, 7, 8, message));
        } else {
            LostItemChatRoomInfoRepository rooms = mock(LostItemChatRoomInfoRepository.class);
            when(rooms.getByArticleIdAndChatRoomId(10, 2)).thenReturn(LostItemChatRoomInfoEntity.builder()
                .articleId(10).chatRoomId(2).authorId(7).ownerId(8).build());
            WebSocketUserSessionService sessions = mock(WebSocketUserSessionService.class);
            when(sessions.read(8)).thenReturn(Optional.empty());
            LostItemChatRoomInfoService roomInfo = mock(LostItemChatRoomInfoService.class);
            when(roomInfo.getAllChatRoomInfo(8)).thenReturn(List.of());
            new ChatMessageSendService(notifications, factory, subscriptions, rooms, users, sessions,
                mock(SimpMessageSendingOperations.class), roomInfo)
                .handle(MessageSendEvent.from(10, 2, 7, message));
        }

        ArgumentCaptor<List<FcmSendRequest>> sent = ArgumentCaptor.forClass(List.class);
        verify(fcm).sendMessages(sent.capture());
        assertThat(sent.getValue()).singleElement().satisfies(request -> {
            assertThat(request.title()).isEqualTo(expectedTitle);
            assertThat(request.content()).isEqualTo("저장된 메시지");
            assertThat(request.path()).isEqualTo(MobileAppPath.CHAT);
            assertThat(request.schemeUri()).isEqualTo("chat?articleId=10&chatRoomId=2");
            assertThat(request.type()).isEqualTo("message");
            assertThat(request.roomId()).isEqualTo("lost-item-10-2");
        });
        verify(history).batchInsert(argThat(records -> records.size() == 1 && records.get(0).getPushSuccess()));
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void pollingNotificationOnlyRechecksTokenAndSubscription(boolean subscribed) {
        UserRepository users = mock(UserRepository.class);
        NotificationSubscribeRepository subscriptions = mock(NotificationSubscribeRepository.class);
        FcmClient fcm = mock(FcmClient.class);
        NotificationJdbcRepository history = mock(NotificationJdbcRepository.class);
        when(users.getById(7)).thenReturn(User.builder().id(7).nickname("발신자")
            .userType(UserType.STUDENT).isAuthed(true).isDeleted(true).build());
        when(users.getById(8)).thenReturn(User.builder().id(8).deviceToken("test-device")
            .userType(UserType.STUDENT).isAuthed(true).isDeleted(true).build());
        when(subscriptions.existsByUserIdAndSubscribeTypeAndDetailTypeIsNull(8, NotificationSubscribeType.LOST_ITEM_CHAT))
            .thenReturn(subscribed);
        when(fcm.sendMessages(anyList())).thenReturn(List.of(FcmSendResponse.succeeded()));
        var notifications = new NotificationService(fcm, users, history, subscriptions);
        var service = new ChatMessagePollingNotificationSendService(notifications, new NotificationFactory(), users,
            subscriptions);

        service.handle(new PollingMessageSendEvent(10, 2, 7, 8,
            new ChatMessage(7, "발신자", "저장된 메시지", LocalDateTime.now(), false)));

        if (subscribed) {
            verify(fcm).sendMessages(anyList());
        } else {
            verifyNoInteractions(fcm, history);
        }
    }

    private static Stream<Arguments> senderNames() {
        return Stream.of(
            Arguments.of(true, "기존 닉네임", "익명 이름", "기존 닉네임님의 메시지"),
            Arguments.of(true, null, "익명 이름", "익명 이름님의 메시지"),
            Arguments.of(false, null, null, "익명 사용자님의 메시지")
        );
    }
}
