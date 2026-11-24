package in.koreatech.koin.socket.interceptor;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import in.koreatech.koin.global.auth.exception.AuthenticationException;
import in.koreatech.koin.socket.session.service.ChatSocketSessionRegistry;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ConnectInterceptor implements ChannelInterceptor {

    private final ChatSocketSessionRegistry sessions;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        // 원본 접근자를 유지해야 CONNECT/STOMP의 사용자 변경 콜백이 호출된다.
        StompHeaderAccessor headers = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (headers == null) {
            return message;
        }
        String sessionId = headers.getSessionId();
        StompCommand command = headers.getCommand();
        if (command == null) {
            if (headers.getMessageType() == SimpMessageType.HEARTBEAT) {
                sessions.heartbeat(sessionId);
            }
            return message;
        }
        switch (command) {
            case CONNECT, STOMP -> headers.setUser(sessions.connect(headers));
            case SEND -> sessions.validateSend(sessionId, headers.getDestination());
            case SUBSCRIBE -> headers.setHeader(ChatSocketSessionRegistry.SUBSCRIPTION_GENERATION,
                sessions.subscribe(sessionId, headers.getSubscriptionId(), headers.getDestination()));
            case UNSUBSCRIBE -> {
                headers.setHeader(ChatSocketSessionRegistry.SUBSCRIPTION_GENERATION,
                    sessions.unsubscribe(sessionId, headers.getSubscriptionId()));
            }
            case DISCONNECT -> sessions.disconnect(sessionId);
            default -> throw AuthenticationException.withDetail("지원하지 않는 STOMP 명령입니다.");
        }
        if (command != StompCommand.CONNECT && command != StompCommand.STOMP && command != StompCommand.DISCONNECT) {
            // 클라이언트 전송 중에는 heartbeat가 생략되므로 일반 명령도 접속 상태를 갱신한다.
            sessions.heartbeat(sessionId);
        }
        return message;
    }
}
