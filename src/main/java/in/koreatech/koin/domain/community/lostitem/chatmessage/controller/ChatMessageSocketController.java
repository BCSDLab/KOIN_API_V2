package in.koreatech.koin.domain.community.lostitem.chatmessage.controller;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;

import in.koreatech.koin.domain.chat.event.ChatMessageStoredEvent;
import in.koreatech.koin.domain.chat.model.ChatDomainType;
import in.koreatech.koin.domain.chat.model.ChatRoomKey;
import in.koreatech.koin.domain.community.lostitem.chatmessage.dto.ChatMessage;
import in.koreatech.koin.domain.community.lostitem.chatmessage.dto.ChatMessageResponse;
import in.koreatech.koin.domain.community.lostitem.chatmessage.event.MessageSendEvent;
import in.koreatech.koin.domain.community.lostitem.chatmessage.service.ChatMessageService;
import in.koreatech.koin.domain.community.lostitem.chatmessage.util.WebSocketSessionTracker;
import in.koreatech.koin.socket.config.auth.UserPrincipal;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class ChatMessageSocketController {

    private final ChatMessageService chatService;
    private final ApplicationEventPublisher eventPublisher;
    private final WebSocketSessionTracker sessionTracker;
    private final SimpMessageSendingOperations broker;

    /** @deprecated 신규 연동은 /app/chatrooms/{roomId}/messages를 사용합니다. */
    @Deprecated
    @MessageMapping("/chat/{articleId}/{chatRoomId}")
    public void handleChatMessage(@DestinationVariable Integer articleId, @DestinationVariable Integer chatRoomId,
        UserPrincipal principal, ChatMessage message) {
        String destination = "/topic/chat/" + articleId + "/" + chatRoomId;
        var saved = chatService.saveMessage(articleId, chatRoomId, principal.getUserId(),
            sessionTracker.getSubscriptionCount(destination), message);

        // 기존 구독자에게는 Redis 발행 성공 여부와 무관하게 직접 전달한다.
        broker.convertAndSend(destination, new ChatMessageResponse(principal.getUserId(), message.userNickname(),
            message.content(), message.timestamp(), message.isImage()));
        eventPublisher.publishEvent(MessageSendEvent.from(articleId, chatRoomId, principal.getUserId(), message));
        eventPublisher.publishEvent(new ChatMessageStoredEvent(
            new ChatRoomKey(ChatDomainType.LOST_ITEM, articleId, chatRoomId),
            new in.koreatech.koin.domain.chat.dto.ChatMessageResponse(saved.getId().toString(), saved.getUserId(),
                saved.getNickName(), saved.getContents(), saved.getCreatedAt(), saved.getIsImage(),
                Boolean.TRUE.equals(saved.getIsRead()) ? 0 : 1, false)));
    }
}
