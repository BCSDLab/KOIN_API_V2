package in.koreatech.koin.domain.teamrecruitment.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import in.koreatech.koin.domain.teamrecruitment.dto.ChatMessageResponse;
import in.koreatech.koin.domain.teamrecruitment.dto.ChatRoomResponse;
import in.koreatech.koin.domain.teamrecruitment.dto.CreateChatMessageRequest;
import in.koreatech.koin.domain.teamrecruitment.dto.DirectChatRoomResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "3. 원지웅 - 채팅/알림", description = "팀원 모집 채팅 API")
public interface TeamRecruitmentChatApi {

    @Operation(summary = "팀 또는 개인 채팅방 정보 조회")
    ResponseEntity<ChatRoomResponse> getChatRoom(
            Integer userId,
            @PathVariable Integer recruitmentId,
            @PathVariable Integer chatRoomId
    );

    @Operation(summary = "지원자와 개인 채팅방 생성 또는 조회")
    ResponseEntity<DirectChatRoomResponse> getOrCreateDirectChatRoom(
            Integer userId,
            @PathVariable Integer recruitmentId,
            @PathVariable Integer applicationId
    );

    @Operation(summary = "채팅 메시지 조회 (Polling)")
    ResponseEntity<List<ChatMessageResponse>> getMessages(
            Integer userId,
            @PathVariable Integer recruitmentId,
            @PathVariable Integer chatRoomId,
            @RequestParam(required = false) Integer afterMessageId,
            @RequestParam(required = false) Integer beforeMessageId,
            @RequestParam(defaultValue = "100") int limit
    );

    @Operation(summary = "메시지 전송")
    ResponseEntity<ChatMessageResponse> createMessage(
            Integer userId,
            @PathVariable Integer recruitmentId,
            @PathVariable Integer chatRoomId,
            @RequestBody CreateChatMessageRequest request
    );
}
