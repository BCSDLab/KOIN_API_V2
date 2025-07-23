package in.koreatech.koin.socket.interceptor;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import in.koreatech.koin.socket.config.auth.UserPrincipal;
import in.koreatech.koin.socket.session.service.WebSocketUserSessionService;
import in.koreatech.koin.socket.session.model.WebSocketUserSessionStatus;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * WebSocket의 STOMP 연결 해제 요청을 가로채 사용자 세션 관리를 수행하는 인터셉터입니다.
 * <p>
 * 사용자가 WebSocket 연결을 종료할 때 호출되며 해당 사용자의 세션 상태를 비활성화합니다.
 * 연결 해제를 요청할 때, 사용자 정보를 확인하고 사용자 세션의 상태를 {@link WebSocketUserSessionStatus#INACTIVE}로 업데이트합니다.
 * </p>
 *
 * <p>
 * 주요 기능:
 * <ul>
 *     <li>사용자의 세션 상태를 비활성화하여 연결이 종료됨을 반영합니다.</li>
 * </ul>
 * </p>
 */
@Component
@RequiredArgsConstructor
public class DisconnectInterceptor implements ChannelInterceptor {

    private final WebSocketUserSessionService webSocketUserSessionService;

    @Override
    public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.DISCONNECT.equals(accessor.getCommand())) {
            UserPrincipal principal = (UserPrincipal) accessor.getUser();

            if (principal != null) {
                webSocketUserSessionService.updateUserStatus(
                    principal.getUserId(),
                    WebSocketUserSessionStatus.INACTIVE
                );
            }
        }

        return message;
    }
}
