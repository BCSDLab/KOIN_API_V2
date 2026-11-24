package in.koreatech.koin.domain.chat.controller;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

import in.koreatech.koin.domain.chat.dto.ChatReadRequest;
import in.koreatech.koin.domain.chat.dto.CreateChatMessageRequest;
import in.koreatech.koin.domain.chat.service.ChatService;
import in.koreatech.koin.socket.config.auth.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class ChatSocketController {

    private final ChatService chatService;

    @MessageMapping("/chatrooms/{roomId}/messages")
    public void sendMessage(@DestinationVariable String roomId, UserPrincipal principal,
        @Valid CreateChatMessageRequest request) {
        chatService.sendMessage(principal.getUserId(), roomId, request);
    }

    @MessageMapping("/chatrooms/{roomId}/read")
    public void markRead(@DestinationVariable String roomId, UserPrincipal principal,
        @Valid ChatReadRequest request) {
        chatService.markRead(principal.getUserId(), roomId, request.parsedMessageId());
    }
}
