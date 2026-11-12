package in.koreatech.koin.domain.chat.controller;

import static in.koreatech.koin.global.code.ApiResponseCode.*;

import java.util.List;

import org.springframework.http.ResponseEntity;

import in.koreatech.koin.domain.chat.dto.ChatMessageResponse;
import in.koreatech.koin.domain.chat.dto.ChatReadRequest;
import in.koreatech.koin.domain.chat.dto.ChatRoomResponse;
import in.koreatech.koin.domain.chat.dto.CreateChatMessageRequest;
import in.koreatech.koin.domain.chat.model.ChatDomainType;
import in.koreatech.koin.global.code.ApiResponseCodes;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "(Normal) Chat: 공통 채팅", description = "분실물, 콜밴, 팀 모집 채팅을 공통 인터페이스로 제공합니다.")
public interface ChatApi {

    @ApiResponseCodes({OK, UNAUTHORIZED_USER, FORBIDDEN_USER_TYPE, UNSUPPORTED_LANGUAGE})
    @Operation(summary = "내 채팅방 목록 조회",
        description = "참여 중인 채팅방을 최근 메시지 순으로 조회합니다. 콜밴과 팀 모집 채팅은 학생 계정만 이용할 수 있습니다.")
    ResponseEntity<List<ChatRoomResponse>> getRooms(Integer userId, ChatDomainType domainType,
        @Parameter(description = "콜밴 방명 표시 언어. 기본 ko-KR이며 사용자 입력은 원문을 유지합니다.", example = "en-US") String language);

    @ApiResponseCodes({OK, UNAUTHORIZED_USER, FORBIDDEN_USER_TYPE, ILLEGAL_ARGUMENT,
        FORBIDDEN_BLOCKED_USER, FORBIDDEN_PARTICIPANT, TEAM_RECRUITMENT_CHAT_FORBIDDEN,
        NOT_FOUND_LOST_ITEM_CHATROOM, TEAM_RECRUITMENT_CHAT_NOT_FOUND, UNSUPPORTED_LANGUAGE})
    @Operation(summary = "채팅방 상세 조회",
        description = "채팅방 정보와 메시지 전송 및 읽음 처리 가능 여부를 조회합니다.")
    ResponseEntity<ChatRoomResponse> getRoom(Integer userId, String roomId,
        @Parameter(description = "콜밴 방명 표시 언어. 기본 ko-KR이며 사용자 입력은 원문을 유지합니다.", example = "en-US") String language);

    @ApiResponseCodes({OK, UNAUTHORIZED_USER, ILLEGAL_ARGUMENT, FORBIDDEN_BLOCKED_USER,
        FORBIDDEN_PARTICIPANT, TEAM_RECRUITMENT_CHAT_FORBIDDEN})
    @Operation(summary = "메시지 목록 조회",
        description = "기준 ID를 생략하면 최근 메시지를 조회하며 결과는 메시지 ID 순으로 반환합니다. 조회 기준은 `after_message_id`와 `before_message_id` 중 하나만 사용합니다.\n\n"
            + "읽음 처리와 접속 상태 갱신은 별도로 요청합니다.")
    ResponseEntity<List<ChatMessageResponse>> getMessages(
        Integer userId, String roomId, Long afterMessageId, Long beforeMessageId, int limit);

    @ApiResponseCodes({OK, UNAUTHORIZED_USER, ILLEGAL_ARGUMENT, INVALID_REQUEST_BODY,
        FORBIDDEN_BLOCKED_USER, FORBIDDEN_PARTICIPANT, TEAM_RECRUITMENT_CHAT_FORBIDDEN})
    @Operation(summary = "메시지 전송",
        description = "`content`에 텍스트나 업로드한 이미지 URL을 넣습니다. `is_image`는 텍스트면 `false`, 이미지면 `true`로 보냅니다.")
    ResponseEntity<ChatMessageResponse> sendMessage(
        Integer userId, String roomId, CreateChatMessageRequest request);

    @ApiResponseCodes({NO_CONTENT, UNAUTHORIZED_USER, ILLEGAL_ARGUMENT, INVALID_REQUEST_BODY})
    @Operation(summary = "메시지 읽음 처리",
        description = "화면에서 확인한 `message_id`까지 읽음 처리합니다. 분실물, 콜밴, 팀 모집에서 같은 방식으로 사용합니다.")
    ResponseEntity<Void> markRead(Integer userId, String roomId, ChatReadRequest request);

    @ApiResponseCodes({NO_CONTENT, UNAUTHORIZED_USER, ILLEGAL_ARGUMENT})
    @Operation(summary = "채팅방 접속 상태 갱신",
        description = "채팅방을 보고 있다는 상태를 서버에 알릴 때 사용합니다.\n\n"
            + "- **사용할 때:** 화면을 보는 동안 본문 없이 약 10초마다 호출합니다.\n- **사용하지 않을 때:** 기존 알림 정책을 따릅니다. 마지막 호출 후 20초가 지나면 폴링 접속 상태가 해제됩니다.\n- **화면을 닫을 때:** `DELETE /chatrooms/{roomId}/presence`로 접속 상태를 즉시 해제합니다.\n\n"
            + "분실물은 해당 방에 접속 중이면 푸시를 보내지 않습니다. 콜밴과 팀 모집은 접속 여부를 푸시 발송 조건에 사용하지 않습니다. 웹소켓은 구독 상태로 자동 관리하며 읽음 처리는 `/read`로 요청합니다.")
    ResponseEntity<Void> touchPresence(Integer userId, String roomId);

    @ApiResponseCodes({NO_CONTENT, UNAUTHORIZED_USER, ILLEGAL_ARGUMENT})
    @Operation(summary = "채팅방 접속 상태 해제",
        description = "접속 상태 갱신 API를 사용했다면 채팅방 화면을 닫을 때 본문 없이 호출합니다.\n\n"
            + "- **호출하면:** 폴링 접속 상태를 즉시 해제합니다. 이후 분실물 푸시는 남아 있는 접속 상태와 기존 발송 조건을 따릅니다.\n- **호출하지 않아도:** 마지막 갱신 후 20초가 지나면 접속 상태가 자동으로 해제됩니다.\n\n"
            + "채팅방 참여는 유지됩니다. 같은 계정으로 이 방을 연 다른 폴링 탭의 접속 상태도 함께 해제됩니다.")
    ResponseEntity<Void> leavePresence(Integer userId, String roomId);
}
