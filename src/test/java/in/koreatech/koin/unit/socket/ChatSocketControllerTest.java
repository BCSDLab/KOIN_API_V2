package in.koreatech.koin.unit.socket;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.messaging.simp.SimpMessageSendingOperations;

import in.koreatech.koin.domain.chat.event.ChatMessageStoredEvent;
import in.koreatech.koin.domain.community.lostitem.chatmessage.controller.ChatMessageSocketController;
import in.koreatech.koin.domain.community.lostitem.chatmessage.dto.ChatMessage;
import in.koreatech.koin.domain.community.lostitem.chatmessage.dto.ChatMessageResponse;
import in.koreatech.koin.domain.community.lostitem.chatmessage.event.MessageSendEvent;
import in.koreatech.koin.domain.community.lostitem.chatmessage.model.ChatMessageEntity;
import in.koreatech.koin.domain.community.lostitem.chatmessage.repository.ChatMessageRedisRepository;
import in.koreatech.koin.domain.community.lostitem.chatmessage.service.ChatMessageService;
import in.koreatech.koin.domain.community.lostitem.chatmessage.util.WebSocketSessionTracker;
import in.koreatech.koin.domain.community.lostitem.chatroom.service.LostItemChatAccessService;
import in.koreatech.koin.socket.config.auth.UserPrincipal;

class ChatSocketControllerTest {
    @Test
    void legacySocketDeliversLocallyBeforePublishingCommonEvent() {
        var service = mock(ChatMessageService.class);
        var events = mock(ApplicationEventPublisher.class);
        var tracker = mock(WebSocketSessionTracker.class);
        var broker = mock(SimpMessageSendingOperations.class);
        var controller = new ChatMessageSocketController(service, events, tracker, broker);
        var time = LocalDateTime.of(2026, 1, 1, 0, 0);
        var message = new ChatMessage(7, "client nickname", "hello", time, false);
        var stored = ChatMessageEntity.create(1, 2, 7, false, message, 100L);
        when(tracker.getSubscriptionCount("/topic/chat/1/2")).thenReturn(1);
        when(service.saveMessage(1, 2, 7, 1, message)).thenReturn(stored);

        controller.handleChatMessage(1, 2, new UserPrincipal(7, "server", null, null), message);

        var order = inOrder(service, broker, events);
        order.verify(service).saveMessage(1, 2, 7, 1, message);
        order.verify(broker).convertAndSend("/topic/chat/1/2", ChatMessageResponse.toResponse(message));
        order.verify(events).publishEvent(MessageSendEvent.from(1, 2, 7, message));
        order.verify(events).publishEvent(argThat((Object event) -> event instanceof ChatMessageStoredEvent saved
            && !saved.relayLegacy() && saved.message().messageId().equals("100")));
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3})
    void legacySocketReadUsesOriginalSubscriptionCount(int subscriptions) {
        var repository = mock(ChatMessageRedisRepository.class);
        var access = mock(LostItemChatAccessService.class);
        var service = new ChatMessageService(repository, access);
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        var message = new ChatMessage(7, "client nickname", " ", null, false);

        var stored = service.saveMessage(1, 2, 7, subscriptions, message);

        assertThat(stored.getIsRead()).isEqualTo(subscriptions == 2);
        assertThat(stored.getNickName()).isEqualTo("client nickname");
        assertThat(stored.getContents()).isEqualTo(" ");
        verify(access).validateParticipant(1, 2, 7);
    }
}
