package in.koreatech.koin.domain.community.lostitem.chatroom.controller;

import static in.koreatech.koin.domain.user.model.UserType.*;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin._common.auth.Auth;
import in.koreatech.koin.domain.community.lostitem.chatmessage.dto.ChatMessageResponse;
import in.koreatech.koin.domain.community.lostitem.chatroom.dto.ChatRoomInfoResponse;
import in.koreatech.koin.domain.community.lostitem.chatroom.dto.ChatRoomListResponse;
import in.koreatech.koin.domain.community.lostitem.chatroom.usecase.LostItemChatRoomMessageUseCase;
import in.koreatech.koin.domain.community.lostitem.chatroom.usecase.LostItemChatRoomUseCase;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/chatroom")
public class LostItemChatRoomController implements LostItemChatRoomApi {

    private final LostItemChatRoomUseCase lostItemChatRoomUseCase;
    private final LostItemChatRoomMessageUseCase lostItemChatRoomMessageUseCase;

    @PostMapping("/lost-item/{articleId}")
    public ResponseEntity<ChatRoomInfoResponse> createLostItemChatRoom(
        @Auth(permit= {GENERAL, STUDENT, COUNCIL}) Integer userId,
        @PathVariable("articleId") Integer articleId
    ) {
        ChatRoomInfoResponse response = lostItemChatRoomUseCase.getOrCreateLostItemChatRoom(articleId, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/lost-item/{articleId}/{chatRoomId}")
    public ResponseEntity<ChatRoomInfoResponse> getLostItemChatRoom(
        @Auth(permit= {GENERAL, STUDENT, COUNCIL}) Integer userId,
        @PathVariable("articleId") Integer articleId,
        @PathVariable("chatRoomId") Integer chatRoomId
    ) {
        ChatRoomInfoResponse response = lostItemChatRoomUseCase.getLostItemChatRoom(articleId, userId, chatRoomId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/lost-item/{articleId}/{chatRoomId}/messages")
    public ResponseEntity<List<ChatMessageResponse>> getAllMessages(
        @Auth(permit= {GENERAL, STUDENT, COUNCIL}) Integer userId,
        @PathVariable("articleId") Integer articleId,
        @PathVariable("chatRoomId") Integer chatRoomId
    ) {
        var result = lostItemChatRoomMessageUseCase.getAllChatRoomMessages(articleId, chatRoomId, userId);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/lost-item")
    public ResponseEntity<List<ChatRoomListResponse>> getAllChatRoomInfo(
        @Auth(permit= {GENERAL, STUDENT, COUNCIL}) Integer userId
    ) {
        return ResponseEntity.ok(lostItemChatRoomUseCase.getAllChatRoomInfoByUserId(userId));
    }
}
