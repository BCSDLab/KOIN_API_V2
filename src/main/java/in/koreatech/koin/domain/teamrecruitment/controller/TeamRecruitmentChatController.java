package in.koreatech.koin.domain.teamrecruitment.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin.domain.teamrecruitment.dto.ChatMessageResponse;
import in.koreatech.koin.domain.teamrecruitment.dto.ChatRoomResponse;
import in.koreatech.koin.domain.teamrecruitment.dto.CreateChatMessageRequest;
import in.koreatech.koin.domain.teamrecruitment.dto.DirectChatRoomCreationResult;
import in.koreatech.koin.domain.teamrecruitment.dto.DirectChatRoomResponse;
import in.koreatech.koin.domain.teamrecruitment.service.TeamRecruitmentChatService;
import in.koreatech.koin.global.auth.Auth;

import static in.koreatech.koin.domain.user.model.UserType.STUDENT;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/chatroom/team-recruitment/{recruitmentId}")
public class TeamRecruitmentChatController implements TeamRecruitmentChatApi {

    private final TeamRecruitmentChatService chatService;

    @GetMapping("/{chatRoomId}")
    public ResponseEntity<ChatRoomResponse> getChatRoom(
            @Auth(permit = {STUDENT}) Integer userId,
            @PathVariable Integer recruitmentId,
            @PathVariable Integer chatRoomId
    ) {
        return ResponseEntity.ok(chatService.getChatRoom(userId, recruitmentId, chatRoomId));
    }

    @PostMapping("/applications/{applicationId}/direct")
    public ResponseEntity<DirectChatRoomResponse> getOrCreateDirectChatRoom(
            @Auth(permit = {STUDENT}) Integer userId,
            @PathVariable Integer recruitmentId,
            @PathVariable Integer applicationId
    ) {
        DirectChatRoomCreationResult result = chatService.getOrCreateDirectChatRoom(userId, recruitmentId, applicationId);
        HttpStatus status = result.isNew() ? HttpStatus.CREATED : HttpStatus.OK;
        return ResponseEntity.status(status).body(result.response());
    }

    @GetMapping("/{chatRoomId}/messages")
    public ResponseEntity<List<ChatMessageResponse>> getMessages(
            @Auth(permit = {STUDENT}) Integer userId,
            @PathVariable Integer recruitmentId,
            @PathVariable Integer chatRoomId,
            @RequestParam(name = "afterMessageId", required = false) Integer afterMessageId,
            @RequestParam(name = "beforeMessageId", required = false) Integer beforeMessageId,
            @RequestParam(name = "limit", defaultValue = "100") int limit
    ) {
        return ResponseEntity.ok(chatService.getMessages(userId, recruitmentId, chatRoomId, afterMessageId, beforeMessageId, limit));
    }

    @PostMapping("/{chatRoomId}/messages")
    public ResponseEntity<ChatMessageResponse> createMessage(
            @Auth(permit = {STUDENT}) Integer userId,
            @PathVariable Integer recruitmentId,
            @PathVariable Integer chatRoomId,
            @Valid @RequestBody CreateChatMessageRequest request
    ) {
        return ResponseEntity.ok(chatService.createMessage(userId, recruitmentId, chatRoomId, request));
    }
}
