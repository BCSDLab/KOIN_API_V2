package in.koreatech.koin.socket.interceptor;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import in.koreatech.koin.socket.config.auth.UserPrincipal;
import in.koreatech.koin.socket.session.model.WebSocketUserSessionStatus;
import in.koreatech.koin.socket.session.service.WebSocketUserSessionService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * WebSocket의 STOMP 구독 해제 요청을 가로채 사용자 세션 관리를 수행하는 인터셉터입니다.
 * <p>
 * 사용자가 특정 채널을 구독 해제할 때 호출되며, 레디스에 저장되어 있는 해당 사용자의 세션 상태를 업데이트합니다.
 * <p>
 * 웹 소켓 세션이 연결된 상태 에서 stomp 경로 구독이 해제된 상태 입니다.
 * <p>
 * 레디스에 저장된 세션의 상태를 {@link WebSocketUserSessionStatus#ACTIVE_APP}으로 업데이트 합니다.
 */
@Component
@RequiredArgsConstructor
public class UnsubscribeInterceptor implements ChannelInterceptor {

    private final WebSocketUserSessionService webSocketUserSessionService;

    @Override
    public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.UNSUBSCRIBE.equals(accessor.getCommand())) {
            UserPrincipal principal = (UserPrincipal) accessor.getUser();

            if (principal != null) {
                webSocketUserSessionService.updateUserStatus(
                    principal.getUserId(),
                    WebSocketUserSessionStatus.ACTIVE_APP
                );
            }
        }

        return message;
    }
}
