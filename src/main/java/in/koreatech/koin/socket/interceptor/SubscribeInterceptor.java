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
import in.koreatech.koin.socket.util.SubscribeDestination;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * WebSocket의 STOMP 구독 요청을 가로채 사용자 세션 관리를 수행하는 인터셉터입니다.
 * <p>
 * 사용자가 특정 채널을 구독할 때 호출되며, 레디스에 저장되어 있는 해당 사용자의 세션 상태를 업데이트합니다.
 * <p>
 * 구독 요청을 받은 후, 사용자의 ID와 디바이스 토큰을 기반으로 사용자의 세션 상태를 업데이트하며
 * <p>
 * 레디스에 저장된 세션의 상태를 {@link WebSocketUserSessionStatus#ACTIVE_CHAT_ROOM}으로 업데이트 합니다.
 */
@Component
@RequiredArgsConstructor
public class SubscribeInterceptor implements ChannelInterceptor {

    private final WebSocketUserSessionService webSocketUserSessionService;

    @Override
    public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            UserPrincipal principal = (UserPrincipal) accessor.getUser();

            if (principal != null) {
                updateUserSessionByDestination(accessor, principal.getUserId());
            }
        }

        return message;
    }

    private void updateUserSessionByDestination(StompHeaderAccessor accessor, Integer userId) {
        if (SubscribeDestination.isChatRoomSubscribe(accessor)) {
            SubscribeDestination destination = SubscribeDestination.extractDestinationInfo(accessor);
            webSocketUserSessionService.updateUserStatus(userId, destination.articleId(), destination.chatRoomId());
            return;
        }

        if (SubscribeDestination.isChatRoomListSubscribe(accessor)) {
            webSocketUserSessionService.updateUserStatus(userId, WebSocketUserSessionStatus.ACTIVE_CHAT_ROOM_LIST);
        }
    }
}
