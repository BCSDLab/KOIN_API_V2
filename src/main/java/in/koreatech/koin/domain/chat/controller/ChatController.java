package in.koreatech.koin.domain.chat.controller;

import static in.koreatech.koin.domain.user.model.UserType.COUNCIL;
import static in.koreatech.koin.domain.user.model.UserType.GENERAL;
import static in.koreatech.koin.domain.user.model.UserType.STUDENT;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin.domain.chat.dto.ChatMessageQuery;
import in.koreatech.koin.domain.chat.dto.ChatMessageResponse;
import in.koreatech.koin.domain.chat.dto.ChatReadRequest;
import in.koreatech.koin.domain.chat.dto.ChatRoomResponse;
import in.koreatech.koin.domain.chat.dto.CreateChatMessageRequest;
import in.koreatech.koin.domain.chat.model.ChatDomainType;
import in.koreatech.koin.domain.chat.service.ChatService;
import in.koreatech.koin.domain.language.model.SupportedLanguage;
import in.koreatech.koin.global.auth.Auth;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/chatrooms")
public class ChatController implements ChatApi {

    private final ChatService chatService;

    @GetMapping
    public ResponseEntity<List<ChatRoomResponse>> getRooms(
        @Auth(permit = {GENERAL, STUDENT, COUNCIL}) Integer userId,
        @RequestParam(name = "domain_type", required = false) ChatDomainType domainType,
        @RequestParam(defaultValue = "ko-KR") String language
    ) {
        return ResponseEntity.ok(chatService.getRooms(userId, domainType, SupportedLanguage.fromCode(language)));
    }

    @GetMapping("/{roomId}")
    public ResponseEntity<ChatRoomResponse> getRoom(
        @Auth(permit = {GENERAL, STUDENT, COUNCIL}) Integer userId,
        @PathVariable String roomId,
        @RequestParam(defaultValue = "ko-KR") String language
    ) {
        return ResponseEntity.ok(chatService.getRoom(userId, roomId, SupportedLanguage.fromCode(language)));
    }

    @GetMapping("/{roomId}/messages")
    public ResponseEntity<List<ChatMessageResponse>> getMessages(
        @Auth(permit = {GENERAL, STUDENT, COUNCIL}) Integer userId,
        @PathVariable String roomId,
        @RequestParam(name = "after_message_id", required = false) Long afterMessageId,
        @RequestParam(name = "before_message_id", required = false) Long beforeMessageId,
        @RequestParam(defaultValue = "100") int limit
    ) {
        ChatMessageQuery query = new ChatMessageQuery(afterMessageId, beforeMessageId, limit);
        return ResponseEntity.ok(chatService.getMessages(userId, roomId, query));
    }

    @PostMapping("/{roomId}/messages")
    public ResponseEntity<ChatMessageResponse> sendMessage(
        @Auth(permit = {GENERAL, STUDENT, COUNCIL}) Integer userId,
        @PathVariable String roomId,
        @Valid @RequestBody CreateChatMessageRequest request
    ) {
        return ResponseEntity.ok(chatService.sendMessage(userId, roomId, request));
    }

    @PostMapping("/{roomId}/read")
    public ResponseEntity<Void> markRead(
        @Auth(permit = {GENERAL, STUDENT, COUNCIL}) Integer userId,
        @PathVariable String roomId,
        @Valid @RequestBody ChatReadRequest request
    ) {
        chatService.markRead(userId, roomId, request.parsedMessageId());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{roomId}/presence")
    public ResponseEntity<Void> touchPresence(
        @Auth(permit = {GENERAL, STUDENT, COUNCIL}) Integer userId,
        @PathVariable String roomId
    ) {
        chatService.touchPresence(userId, roomId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{roomId}/presence")
    public ResponseEntity<Void> leavePresence(
        @Auth(permit = {GENERAL, STUDENT, COUNCIL}) Integer userId,
        @PathVariable String roomId
    ) {
        chatService.leavePresence(userId, roomId);
        return ResponseEntity.noContent().build();
    }
}
