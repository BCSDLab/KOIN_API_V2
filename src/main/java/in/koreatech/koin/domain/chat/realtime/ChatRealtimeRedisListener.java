package in.koreatech.koin.domain.chat.realtime;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import in.koreatech.koin.domain.chat.dto.ChatRealtimeEvent;
import in.koreatech.koin.domain.chat.event.ChatRealtimeReceivedEvent;
import in.koreatech.koin.domain.chat.model.ChatRoomKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatRealtimeRedisListener implements MessageListener {
    private final ObjectMapper objectMapper;
    private final ApplicationEventPublisher events;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            var payload = objectMapper.readTree(message.getBody());
            if (!(payload instanceof ObjectNode object)) {
                return;
            }
            boolean relayLegacy = object.path("relay_legacy").asBoolean(false);
            object.remove("relay_legacy");
            ChatRealtimeEvent event = objectMapper.treeToValue(object, ChatRealtimeEvent.class);
            if (event == null || event.type() == null
                || (event.type() == ChatRealtimeEvent.Type.MESSAGE_CREATED && event.message() == null)) {
                log.warn("Invalid chat realtime envelope");
                return;
            }
            ChatRoomKey.parse(event.roomId());
            events.publishEvent(new ChatRealtimeReceivedEvent(event, relayLegacy));
        } catch (Exception exception) {
            // Do not log payloads or exception messages, which may contain private message content.
            log.warn("Chat realtime receive failed: error={}", exception.getClass().getSimpleName());
        }
    }
}
