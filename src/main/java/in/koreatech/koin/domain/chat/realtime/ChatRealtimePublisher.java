package in.koreatech.koin.domain.chat.realtime;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import in.koreatech.koin.domain.chat.dto.ChatRealtimeEvent;
import in.koreatech.koin.domain.chat.event.ChatMessageStoredEvent;
import in.koreatech.koin.domain.chat.event.ChatRoomChangedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatRealtimePublisher {
    public static final String CHANNEL = "chat:realtime:v1";

    private final StringRedisTemplate redis;
    private final ObjectMapper objectMapper;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onMessageStored(ChatMessageStoredEvent event) {
        publish(new ChatRealtimeEvent(ChatRealtimeEvent.Type.MESSAGE_CREATED, event.room().encoded(), event.message()),
            event.relayLegacy());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onRoomChanged(ChatRoomChangedEvent event) {
        publish(new ChatRealtimeEvent(ChatRealtimeEvent.Type.ROOM_CHANGED, event.room().encoded(), null), false);
    }

    private void publish(ChatRealtimeEvent event, boolean relayLegacy) {
        try {
            ObjectNode payload = objectMapper.valueToTree(event);
            if (relayLegacy) {
                // Redis 내부 전달 정보이며 클라이언트 응답에는 포함하지 않는다.
                payload.put("relay_legacy", true);
            }
            redis.convertAndSend(CHANNEL, objectMapper.writeValueAsString(payload));
        } catch (JsonProcessingException | RuntimeException exception) {
            // Persistence already succeeded; clients recover a missed notification through polling.
            log.warn("Chat fanout failed: room={}, type={}, error={}", event.roomId(), event.type(),
                exception.getClass().getSimpleName());
        }
    }
}
