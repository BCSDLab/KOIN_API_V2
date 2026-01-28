package in.koreatech.koin.domain.community.lostitem.chatmessage.controller;

import static in.koreatech.koin.domain.user.model.UserType.*;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin.domain.community.lostitem.chatmessage.dto.ChatMessageRequest;
import in.koreatech.koin.domain.community.lostitem.chatmessage.dto.ChatMessageResponse;
import in.koreatech.koin.domain.community.lostitem.chatmessage.service.ChatMessagePollingService;
import in.koreatech.koin.global.auth.Auth;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v2/chatroom/")
public class ChatMessagePollingController implements ChatMessagePollingApi {

    private final ChatMessagePollingService chatMessagePollingService;

    @GetMapping("/lost-item/{articleId}/{chatRoomId}/messages")
    public ResponseEntity<List<ChatMessageResponse>> pollAllMessages(
        @Auth(permit = {GENERAL, STUDENT, COUNCIL}) Integer userId,
        @PathVariable("articleId") Integer articleId,
        @PathVariable("chatRoomId") Integer chatRoomId
    ) {
        List<ChatMessageResponse> messages = chatMessagePollingService.pollAllMessages(articleId, chatRoomId, userId);
        return ResponseEntity.ok(messages);
    }

    @PostMapping("/lost-item/{articleId}/{chatRoomId}/messages")
    public ResponseEntity<ChatMessageResponse> sendMessage(
        @Auth(permit = {GENERAL, STUDENT, COUNCIL}) Integer userId,
        @PathVariable("articleId") Integer articleId,
        @PathVariable("chatRoomId") Integer chatRoomId,
        @RequestBody ChatMessageRequest request
    ) {
        ChatMessageResponse response = chatMessagePollingService.sendMessage(articleId, chatRoomId, userId,
            request.toChatMessage(userId));
        return ResponseEntity.ok(response);
    }

    @PostMapping("/lost-item/{articleId}/{chatRoomId}/leave")
    public ResponseEntity<Void> leaveRoom(
        @Auth(permit = {GENERAL, STUDENT, COUNCIL}) Integer userId,
        @PathVariable("articleId") Integer articleId,
        @PathVariable("chatRoomId") Integer chatRoomId
    ) {
        chatMessagePollingService.leaveRoom(articleId, chatRoomId, userId);
        return ResponseEntity.ok().build();
    }
}
