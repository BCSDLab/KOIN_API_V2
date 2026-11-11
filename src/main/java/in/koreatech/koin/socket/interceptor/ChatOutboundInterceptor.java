package in.koreatech.koin.socket.interceptor;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

import in.koreatech.koin.socket.session.service.ChatSocketSessionRegistry;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ChatOutboundInterceptor implements ChannelInterceptor {

    private final ChatSocketSessionRegistry sessions;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        if (SimpMessageHeaderAccessor.getMessageType(message.getHeaders()) != SimpMessageType.MESSAGE) {
            return message;
        }
        // Spring 6.0의 simpUser는 송신자일 수 있으므로 수신자는 세션 ID로 확인한다.
        return sessions.canDeliver(SimpMessageHeaderAccessor.getSessionId(message.getHeaders()),
            SimpMessageHeaderAccessor.getSubscriptionId(message.getHeaders()),
            SimpMessageHeaderAccessor.getDestination(message.getHeaders())) ? message : null;
    }
}
