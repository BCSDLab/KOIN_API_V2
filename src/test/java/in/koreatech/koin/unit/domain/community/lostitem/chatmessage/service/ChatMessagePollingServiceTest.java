package in.koreatech.koin.unit.domain.community.lostitem.chatmessage.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import in.koreatech.koin.domain.chat.event.ChatMessageStoredEvent;
import in.koreatech.koin.domain.chat.service.ChatPresenceService;
import in.koreatech.koin.domain.community.lostitem.chatmessage.dto.ChatMessage;
import in.koreatech.koin.domain.community.lostitem.chatmessage.event.PollingMessageSendEvent;
import in.koreatech.koin.domain.community.lostitem.chatmessage.model.ChatMessageEntity;
import in.koreatech.koin.domain.community.lostitem.chatmessage.repository.ChatMessageRedisRepository;
import in.koreatech.koin.domain.community.lostitem.chatmessage.repository.ChatRoomPollingSessionRedisRepository;
import in.koreatech.koin.domain.community.lostitem.chatmessage.service.ChatMessagePollingService;
import in.koreatech.koin.domain.community.lostitem.chatroom.model.LostItemChatRoomInfoEntity;
import in.koreatech.koin.domain.community.lostitem.chatroom.service.LostItemChatAccessService;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.repository.UserRepository;
import in.koreatech.koin.global.code.ApiResponseCode;
import in.koreatech.koin.global.exception.CustomException;

@ExtendWith(MockitoExtension.class)
class ChatMessagePollingServiceTest {
    @Mock private ChatMessageRedisRepository messages;
    @Mock private ChatRoomPollingSessionRedisRepository sessions;
    @Mock private LostItemChatAccessService access;
    @Mock private ChatPresenceService presence;
    @Mock private UserRepository users;
    @Mock private ApplicationEventPublisher events;
    @InjectMocks private ChatMessagePollingService service;

    @Test
    void canonicalSavedEntityDrivesResponseAndOneRealtimeEventAndExistingNotification() {
        when(access.validateAccess(100, 1, 20)).thenReturn(LostItemChatRoomInfoEntity.toEntity(100, 1, 20, 10));
        User user = mock(User.class);
        when(users.getById(20)).thenReturn(user);
        when(user.getDisplayNickname()).thenReturn("server nickname");
        var stored = ChatMessageEntity.builder().id(9007199254740993L).articleId(100).chatRoomId(1)
            .userId(20).nickName("server nickname").contents("hello").isImage(false).isRead(false)
            .createdAt(LocalDateTime.of(2026, 10, 12, 10, 0)).build();
        when(messages.save(any())).thenReturn(stored);
        var response = service.sendMessageEntity(100, 1, 20,
            new ChatMessage(99, "forged nickname", "hello", LocalDateTime.MIN, false));
        assertThat(response).isSameAs(stored);
        ArgumentCaptor<ChatMessageEntity> input = ArgumentCaptor.forClass(ChatMessageEntity.class);
        verify(messages).save(input.capture());
        assertThat(input.getValue().getUserId()).isEqualTo(20);
        assertThat(input.getValue().getNickName()).isEqualTo("server nickname");
        ArgumentCaptor<Object> published = ArgumentCaptor.forClass(Object.class);
        verify(events, times(2)).publishEvent(published.capture());
        assertThat(published.getAllValues()).filteredOn(ChatMessageStoredEvent.class::isInstance).hasSize(1);
        var event = (ChatMessageStoredEvent) published.getAllValues().get(0);
        assertThat(event.message().messageId()).isEqualTo(stored.getId().toString());
        assertThat(event.message().timestamp()).isEqualTo(stored.getCreatedAt());
        assertThat(event.relayLegacy()).isTrue();
        assertThat(published.getAllValues().get(1)).isInstanceOf(PollingMessageSendEvent.class);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void legacyRestUsesOnlyPollingAndPreservesClientNicknameAndBlankContent(boolean polling) {
        when(access.validateParticipant(100, 1, 20)).thenReturn(LostItemChatRoomInfoEntity.toEntity(100, 1, 20, 10));
        when(sessions.isUserOnline(100, 1, 10)).thenReturn(polling);
        when(messages.save(any())).thenAnswer(invocation ->
            ((ChatMessageEntity) invocation.getArgument(0)).toBuilder().id(9007199254740993L).build());
        var saved = service.sendLegacyMessageEntity(100, 1, 20,
            new ChatMessage(99, "client nickname", " ", LocalDateTime.MIN, null));
        assertThat(saved.getIsRead()).isEqualTo(polling);
        assertThat(saved.getNickName()).isEqualTo("client nickname");
        assertThat(saved.getUserId()).isEqualTo(20);
        assertThat(saved.getContents()).isEqualTo(" ");
        ArgumentCaptor<ChatMessageStoredEvent> event = ArgumentCaptor.forClass(ChatMessageStoredEvent.class);
        verify(events).publishEvent(event.capture());
        assertThat(event.getValue().relayLegacy()).isFalse();
        verifyNoInteractions(presence, users);
        verify(events, times(polling ? 0 : 1)).publishEvent(any(PollingMessageSendEvent.class));
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void commonSendUsesPresenceForPushButRequiresExplicitRead(boolean online) {
        when(access.validateAccess(100, 1, 20)).thenReturn(LostItemChatRoomInfoEntity.toEntity(100, 1, 20, 10));
        when(users.getById(20)).thenReturn(mock(User.class));
        when(presence.isOnline(any(), eq(10))).thenReturn(online);
        when(messages.save(any())).thenAnswer(invocation ->
            ((ChatMessageEntity) invocation.getArgument(0)).toBuilder().id(9007199254740993L).build());
        var saved = service.sendMessageEntity(100, 1, 20,
            new ChatMessage(20, "nickname", "hello", LocalDateTime.MIN, false));
        assertThat(saved.getIsRead()).isFalse();
        verifyNoInteractions(sessions);
        verify(events, times(online ? 0 : 1)).publishEvent(any(PollingMessageSendEvent.class));
    }

    @ParameterizedTest
    @MethodSource("invalidMessages")
    void invalidCommonBodiesFailBeforeAnyEffects(ChatMessage message) {
        assertThatThrownBy(() -> service.sendMessageEntity(100, 1, 20, message))
            .isInstanceOfSatisfying(CustomException.class,
                exception -> assertThat(exception.getErrorCode()).isEqualTo(ApiResponseCode.INVALID_REQUEST_BODY));
        verifyNoInteractions(access, messages, sessions, presence, users, events);
    }

    private static Stream<ChatMessage> invalidMessages() {
        return Stream.of(null,
            new ChatMessage(20, null, null, null, false),
            new ChatMessage(20, null, " \t\n", null, true),
            new ChatMessage(20, null, "hello", null, null));
    }

    @Test
    void unauthorizedLegacyReadAndSendHaveNoSideEffects() {
        doThrow(CustomException.of(ApiResponseCode.FORBIDDEN_AUTHOR)).when(access).validateParticipant(100, 1, 99);
        doThrow(CustomException.of(ApiResponseCode.FORBIDDEN_AUTHOR)).when(access).validateAccess(100, 1, 99);
        assertThatThrownBy(() -> service.pollAllMessages(100, 1, 99)).isInstanceOf(CustomException.class);
        assertThatThrownBy(() -> service.sendMessageEntity(100, 1, 99,
            new ChatMessage(20, "nickname", "hello", LocalDateTime.MIN, false))).isInstanceOf(CustomException.class);
        verifyNoInteractions(messages, sessions, presence, users, events);
    }

    @Test
    void blockedParticipantCanReadBothLegacyHistoryPaths() {
        lenient().doThrow(CustomException.of(ApiResponseCode.FORBIDDEN_BLOCKED_USER))
            .when(access).validateAccess(100, 1, 20);
        when(messages.findByArticleIdAndChatRoomId(100, 1)).thenReturn(List.of());
        service.pollAllMessages(100, 1, 20);
        service.markAllMessagesAsRead(100, 1, 20);
        verify(access, times(2)).validateParticipant(100, 1, 20);
        verify(access, never()).validateAccess(anyInt(), anyInt(), anyInt());
        verify(messages, times(2)).markAllMessagesAsRead(100, 1, 20);
    }

    @Test
    void legacyReadStillMarksAllReadAfterAuthorization() {
        when(messages.findByArticleIdAndChatRoomId(100, 1)).thenReturn(List.of());
        service.pollAllMessages(100, 1, 20);
        var order = inOrder(access, sessions, messages);
        order.verify(access).validateParticipant(100, 1, 20);
        order.verify(sessions).markUserOnline(100, 1, 20);
        order.verify(messages).markAllMessagesAsRead(100, 1, 20);
        order.verify(messages).findByArticleIdAndChatRoomId(100, 1);
    }
}
