package in.koreatech.koin.domain.community.lostitem.chatmessage.controller;

import static in.koreatech.koin.domain.user.model.UserType.*;
import static in.koreatech.koin.global.code.ApiResponseCode.OK;
import static in.koreatech.koin.global.code.ApiResponseCode.UNAUTHORIZED_USER;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import in.koreatech.koin.domain.community.lostitem.chatmessage.dto.ChatMessageRequest;
import in.koreatech.koin.domain.community.lostitem.chatmessage.dto.ChatMessageResponse;
import in.koreatech.koin.global.auth.Auth;
import in.koreatech.koin.global.code.ApiResponseCodes;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "(Normal) LostItemChat: 분실물 쪽지 Polling", description = "분실물 쪽지 Polling 채팅 관리")
@RequestMapping("/v2/chatroom/")
public interface ChatMessagePollingApi {

    @ApiResponseCodes({
        OK,
        UNAUTHORIZED_USER,
    })
    @Operation(
        summary = "분실물 게시글 쪽지 채팅방 전체 메시지 조회 (Poll) 및 활성 세션 갱신",
        description = """
            ### 폴링 방식 메시지 조회 API
             이 API는 **10초마다 주기적으로 호출**하여 새로운 메시지를 받아오는 폴링(Polling) 방식으로 동작
             
             1. **메시지 조회**: 해당 채팅방의 모든 메시지를 시간순으로 반환
             2. **자동 읽음 처리**: 상대방이 보낸 안 읽은 메시지들을 자동으로 읽음 처리
             3. **세션 갱신**: 이 API를 호출하는 것 자체가 "현재 채팅방에 접속 중"임을 서버에 알림 (TTL 20초). 마지막 폴링 후 20초 이내에 다시 호출하지 않으면 "오프라인" 상태로 간주됨
            """
    )
    @GetMapping("/lost-item/{articleId}/{chatRoomId}/messages")
    ResponseEntity<List<ChatMessageResponse>> pollAllMessages(
        @Auth(permit = {GENERAL, STUDENT, COUNCIL}) Integer userId,
        @PathVariable("articleId") Integer articleId,
        @PathVariable("chatRoomId") Integer chatRoomId
    );

    @ApiResponseCodes({
        OK,
        UNAUTHORIZED_USER,
    })
    @Operation(
        summary = "분실물 게시글 쪽지 채팅방 메시지 전송",
        description = """
            ### 폴링 방식 메시지 전송 API
            #### 읽음 상태 자동 결정
            - **상대방 온라인** (20초 이내 폴링 기록 있음)
              - `is_read: true` 상태로 저장 → 상대방이 채팅창을 보고 있으므로 즉시 읽음 처리
              - FCM 푸시 알림 발송 안 함
            
            - **상대방 오프라인** (20초 이상 폴링 없음)
              - `is_read: false` 상태로 저장 → 상대방이 아직 확인하지 않음
              - FCM 푸시 알림 자동 발송
            
            #### 주의 사항
            - 메시지 전송 후 UI를 업데이트하려면 폴링 API를 다시 호출해야 합니다
            - 전송 직후에는 일반 폴링 주기를 기다리지 말고 폴링 API를 즉시 한 번 호출해야 합니다.
            """
    )
    @PostMapping("/lost-item/{articleId}/{chatRoomId}/messages")
    ResponseEntity<ChatMessageResponse> sendMessage(
        @Auth(permit = {GENERAL, STUDENT, COUNCIL}) Integer userId,
        @PathVariable("articleId") Integer articleId,
        @PathVariable("chatRoomId") Integer chatRoomId,
        @RequestBody ChatMessageRequest request
    );

    @ApiResponseCodes({
        OK,
        UNAUTHORIZED_USER,
    })
    @Operation(
        summary = "분실물 게시글 쪽지 채팅방 명시적 퇴장",
        description = """
            ### 채팅방 퇴장 API
            채팅방에서 나갈 때 명시적으로 호출하여 즉시 오프라인 상태로 전환하는 API
            
            #### 주요 기능
            - 서버에 저장된 세션 정보를 즉시 삭제
            - 퇴장 후 상대방이 메시지를 보내면 FCM 푸시 알림이 발송됩니다
            
            
            #### 주의 사항
            - 이 API를 호출하지 않아도 20초 후 자동으로 오프라인 처리됨
            - 즉시 오프라인 처리가 필요한 경우 (채팅방 나가기, 다른 채팅방으로 이동 등) 호출을 권장
            """
    )
    @PostMapping("/lost-item/{articleId}/{chatRoomId}/leave")
    ResponseEntity<Void> leaveRoom(
        @Auth(permit = {GENERAL, STUDENT, COUNCIL}) Integer userId,
        @PathVariable("articleId") Integer articleId,
        @PathVariable("chatRoomId") Integer chatRoomId
    );
}
