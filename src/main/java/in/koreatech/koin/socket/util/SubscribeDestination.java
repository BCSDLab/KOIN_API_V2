package in.koreatech.koin.socket.util;

import org.springframework.messaging.simp.stomp.StompHeaderAccessor;

import in.koreatech.koin._common.exception.custom.KoinIllegalArgumentException;


public record SubscribeDestination(
    Long articleId,
    Long chatRoomId
) {

    /**
     * destination 헤더에서 articleId와 chatRoomId를 추출하는 메서드
     *
     * @param accessor StompHeaderAccessor
     * @return DestinationInfo (articleId와 chatRoomId를 포함)
     * /topic/chat/{articleId}/{chatRoomId} 형태
     */
    public static SubscribeDestination extractDestinationInfo(StompHeaderAccessor accessor) {
        String destination = accessor.getDestination();
        if (destination == null || !destination.startsWith("/topic/chat/")) {
            throw new KoinIllegalArgumentException("올바르지 않은 구독 경로입니다: " + destination);
        }

        String[] pathSegments = destination.split("/");

        Long articleId = Long.valueOf(pathSegments[3]);
        Long chatRoomId = Long.valueOf(pathSegments[4]);
        return new SubscribeDestination(articleId, chatRoomId);
    }

    public static boolean isChatRoomSubscribe(StompHeaderAccessor accessor) {
        String destination = accessor.getDestination();
        return destination != null && destination.startsWith("/topic/chat/");
    }

    public static boolean isChatRoomListSubscribe(StompHeaderAccessor accessor) {
        String destination = accessor.getDestination();
        return destination != null && destination.startsWith("/topic/chatroom/list");
    }
}
