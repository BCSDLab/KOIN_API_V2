package in.koreatech.koin.unit.domain.chat.realtime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.transaction.event.TransactionalEventListenerFactory;
import org.springframework.data.redis.connection.DefaultMessage;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.simp.SimpMessageSendingOperations;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import in.koreatech.koin.domain.chat.dto.ChatMessageResponse;
import in.koreatech.koin.domain.chat.dto.ChatRealtimeEvent;
import in.koreatech.koin.domain.chat.event.ChatMessageStoredEvent;
import in.koreatech.koin.domain.chat.event.ChatRealtimeReceivedEvent;
import in.koreatech.koin.domain.chat.event.ChatRoomChangedEvent;
import in.koreatech.koin.domain.chat.model.ChatDomainType;
import in.koreatech.koin.domain.chat.model.ChatRoomKey;
import in.koreatech.koin.domain.chat.realtime.ChatRealtimePublisher;
import in.koreatech.koin.domain.chat.realtime.ChatRealtimeRedisListener;
import in.koreatech.koin.domain.chat.realtime.ChatRealtimeRelay;

class ChatRealtimeRelayTest {
    private final ObjectMapper mapper = JsonMapper.builder().addModule(new JavaTimeModule()).build();
    private final StringRedisTemplate redis = mock(StringRedisTemplate.class);
    private final ChatRoomKey room = new ChatRoomKey(ChatDomainType.LOST_ITEM, 10, 2);
    private final ChatMessageResponse message = new ChatMessageResponse("9007199254740993", 7, "sender",
        "saved content", LocalDateTime.of(2026, 10, 12, 12, 0), false, 1, false);

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void saved_message_publishes_once_and_receiver_only_emits_local_event(boolean relayLegacy) throws Exception {
        new ChatRealtimePublisher(redis, mapper).onMessageStored(new ChatMessageStoredEvent(room, message, relayLegacy));
        ArgumentCaptor<String> json = ArgumentCaptor.forClass(String.class);
        verify(redis).convertAndSend(eq(ChatRealtimePublisher.CHANNEL), json.capture());
        verifyNoMoreInteractions(redis);
        assertThat(mapper.readTree(json.getValue()).path("room_id").asText()).isEqualTo(room.encoded());
        assertThat(mapper.readTree(json.getValue()).path("message").path("message_id").asText())
            .isEqualTo(message.messageId());
        ApplicationEventPublisher events = mock(ApplicationEventPublisher.class);
        new ChatRealtimeRedisListener(mapper, events).onMessage(new DefaultMessage(
            ChatRealtimePublisher.CHANNEL.getBytes(StandardCharsets.UTF_8), json.getValue().getBytes(StandardCharsets.UTF_8)), null);
        var event = new ChatRealtimeEvent(ChatRealtimeEvent.Type.MESSAGE_CREATED, room.encoded(), message);
        verify(events).publishEvent(new ChatRealtimeReceivedEvent(event, relayLegacy));
        verifyNoMoreInteractions(events, redis);
        SimpMessageSendingOperations broker = mock(SimpMessageSendingOperations.class);
        new ChatRealtimeRelay(broker).onReceived(new ChatRealtimeReceivedEvent(event));
        verify(broker).convertAndSend("/topic/chatrooms/" + room.encoded(), event);
        verifyNoMoreInteractions(broker);
    }

    @Test
    void room_change_is_a_single_signal_and_fanout_failure_does_not_escape() throws Exception {
        var publisher = new ChatRealtimePublisher(redis, mapper);
        publisher.onRoomChanged(new ChatRoomChangedEvent(room));
        ArgumentCaptor<String> json = ArgumentCaptor.forClass(String.class);
        verify(redis).convertAndSend(eq(ChatRealtimePublisher.CHANNEL), json.capture());
        assertThat(mapper.readValue(json.getValue(), ChatRealtimeEvent.class))
            .isEqualTo(new ChatRealtimeEvent(ChatRealtimeEvent.Type.ROOM_CHANGED, room.encoded(), null));
        when(redis.convertAndSend(anyString(), anyString())).thenThrow(new IllegalStateException("offline"));
        assertThatCode(() -> publisher.onMessageStored(new ChatMessageStoredEvent(room, message)))
            .doesNotThrowAnyException();
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void transaction_commit_fans_out_once_while_rollback_never_fans_out(boolean readChange) {
        Object event = readChange ? new ChatRoomChangedEvent(room) : new ChatMessageStoredEvent(room, message);
        try (var context = new AnnotationConfigApplicationContext()) {
            context.registerBean(TransactionalEventListenerFactory.class);
            context.registerBean(ChatRealtimePublisher.class, () -> new ChatRealtimePublisher(redis, mapper));
            context.refresh();
            var transactions = new TransactionTemplate(new AbstractPlatformTransactionManager() {
                @Override
                protected Object doGetTransaction() { return new Object(); }
                @Override
                protected void doBegin(Object transaction, TransactionDefinition definition) { }
                @Override
                protected void doCommit(DefaultTransactionStatus status) { }
                @Override
                protected void doRollback(DefaultTransactionStatus status) { }
            });
            transactions.executeWithoutResult(status -> {
                context.publishEvent(event);
                verifyNoInteractions(redis);
            });
            verify(redis).convertAndSend(eq(ChatRealtimePublisher.CHANNEL), anyString());
            clearInvocations(redis);
            transactions.executeWithoutResult(status -> {
                context.publishEvent(event);
                status.setRollbackOnly();
            });
            verifyNoInteractions(redis);
            context.publishEvent(event);
            verify(redis).convertAndSend(eq(ChatRealtimePublisher.CHANNEL), anyString());
        }
    }

    @Test
    void malformed_redis_payload_is_dropped() {
        ApplicationEventPublisher events = mock(ApplicationEventPublisher.class);
        var listener = new ChatRealtimeRedisListener(mapper, events);
        for (String json : new String[] {"invalid", "null", "{}",
            "{\"type\":\"MESSAGE_CREATED\",\"room_id\":\"lost-item-10-2\"}"}) {
            assertThatCode(() -> listener.onMessage(new DefaultMessage(new byte[0],
                json.getBytes(StandardCharsets.UTF_8)), null)).doesNotThrowAnyException();
        }
        verifyNoInteractions(events);
    }
}
