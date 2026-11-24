package in.koreatech.koin.domain.chat.realtime;

import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Component;

import in.koreatech.koin.domain.chat.event.ChatRealtimeReceivedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatRealtimeRelay {
    private final SimpMessageSendingOperations broker;

    @EventListener
    public void onReceived(ChatRealtimeReceivedEvent received) {
        var event = received.event();
        try {
            broker.convertAndSend("/topic/chatrooms/" + event.roomId(), event);
        } catch (RuntimeException exception) {
            log.warn("Chat local relay failed: room={}, type={}, error={}", event.roomId(), event.type(),
                exception.getClass().getSimpleName());
        }
    }
}
