package in.koreatech.koin.socket.domain.message.controller;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;

import in.koreatech.koin.socket.config.auth.UserPrincipal;
import in.koreatech.koin.socket.domain.message.dto.ChatMessageResponse;
import in.koreatech.koin.socket.domain.message.model.ChatMessageCommand;
import in.koreatech.koin.socket.domain.message.service.ChatMessageService;
import in.koreatech.koin.socket.domain.message.service.MessageEventHandler;
import in.koreatech.koin.socket.domain.message.util.WebSocketSessionTracker;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class ChatMessageSocketController {

    private final ChatMessageService chatService;
    private final MessageEventHandler messageEventHandler;
    private final WebSocketSessionTracker sessionTracker;
    private final SimpMessageSendingOperations simpMessageSendingOperations;

    @MessageMapping("/chat/{articleId}/{chatRoomId}")
    public void handleChatMessage(
        @DestinationVariable Integer articleId,
        @DestinationVariable Integer chatRoomId,
        UserPrincipal principal,
        ChatMessageCommand message
    ) {
        String destination = "/topic/chat/" + articleId + "/" + chatRoomId;
        Integer subscriptionCount = sessionTracker.getSubscriptionCount(destination);
        Integer userId = principal.getUserId();

        chatService.saveMessage(articleId, chatRoomId, userId, subscriptionCount, message);

        var sendMessage = ChatMessageResponse.toResponse(message);
        simpMessageSendingOperations.convertAndSend(destination, sendMessage);

        messageEventHandler.onMessageReceived(articleId, chatRoomId, userId, message);
    }
}
