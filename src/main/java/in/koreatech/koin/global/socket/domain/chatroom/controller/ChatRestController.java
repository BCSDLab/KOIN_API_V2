package in.koreatech.koin.global.socket.domain.chatroom.controller;

import static in.koreatech.koin.domain.user.model.UserType.*;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin.global.auth.Auth;
import in.koreatech.koin.global.socket.domain.chatroom.dto.ChatRoomInfoResponse;
import in.koreatech.koin.global.socket.domain.chatroom.dto.ChatRoomListResponse;
import in.koreatech.koin.global.socket.domain.chatroom.service.LostItemChatRoomInfoService;
import in.koreatech.koin.global.socket.domain.message.dto.ChatMessageResponse;
import in.koreatech.koin.global.socket.domain.message.service.ChatMessageService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/chatroom")
public class ChatRestController implements ChatRestApi {

    private final LostItemChatRoomInfoService chatRoomInfoService;
    private final ChatMessageService chatMessageService;

    @PostMapping("/lost-item/{articleId}")
    public ResponseEntity<ChatRoomInfoResponse> createLostItemChatRoom(
        @Auth(permit= {STUDENT}) Integer studentId,
        @PathVariable("articleId") Integer articleId
    ) {
        Integer chatRoomId = chatRoomInfoService.createLostItemChatRoom(articleId, studentId);
        String articleTitle = chatRoomInfoService.getArticleTitle(articleId);
        String chatPartnerProfileImage = chatRoomInfoService.getChatPartnerProfileImage(articleId);
        return ResponseEntity.status(HttpStatus.CREATED).body(
            ChatRoomInfoResponse.from(articleId, chatRoomId, studentId, articleTitle, chatPartnerProfileImage));
    }

    @GetMapping("/lost-item/{articleId}/{chatRoomId}")
    public ResponseEntity<?> getLostItemChatRoom(
        @Auth(permit= {STUDENT, COUNCIL}) Integer studentId,
        @PathVariable("articleId") Integer articleId,
        @PathVariable("chatRoomId") Integer chatRoomId
    ) {
        String articleTitle = chatRoomInfoService.getArticleTitle(articleId);
        String chatPartnerProfileImage = chatRoomInfoService.getChatPartnerProfileImage(articleId);
        return ResponseEntity.ok(
            ChatRoomInfoResponse.from(articleId, chatRoomId, studentId, articleTitle, chatPartnerProfileImage)
        );
    }

    @GetMapping("/lost-item/{articleId}/{chatRoomId}/messages")
    public ResponseEntity<List<ChatMessageResponse>> getAllMessages(
        @Auth(permit= {STUDENT, COUNCIL}) Integer studentId,
        @PathVariable("articleId") Integer articleId,
        @PathVariable("chatRoomId") Integer chatRoomId
    ) {
        var result = chatMessageService.readMessages(articleId, chatRoomId, studentId).stream()
            .map(ChatMessageResponse::toResponse)
            .toList();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/lost-item/")
    public ResponseEntity<List<ChatRoomListResponse>> getAllChatRoomInfo(
        @Auth(permit= {STUDENT, COUNCIL}) Integer studentId
    ) {
        return ResponseEntity.ok(chatRoomInfoService.getAllChatRoomInfo(studentId));
    }
}
