package in.koreatech.koin.domain.teamrecruitment.controller;

import static in.koreatech.koin.global.code.ApiResponseCode.FORBIDDEN_USER_TYPE;
import static in.koreatech.koin.global.code.ApiResponseCode.INVALID_REQUEST_BODY;
import static in.koreatech.koin.global.code.ApiResponseCode.OK;
import static in.koreatech.koin.global.code.ApiResponseCode.TEAM_RECRUITMENT_APPLICATION_NOT_FOUND;
import static in.koreatech.koin.global.code.ApiResponseCode.TEAM_RECRUITMENT_CHAT_FORBIDDEN;
import static in.koreatech.koin.global.code.ApiResponseCode.TEAM_RECRUITMENT_CHAT_NOT_FOUND;
import static in.koreatech.koin.global.code.ApiResponseCode.TEAM_RECRUITMENT_CHAT_READ_ONLY;
import static in.koreatech.koin.global.code.ApiResponseCode.TEAM_RECRUITMENT_FORBIDDEN;
import static in.koreatech.koin.global.code.ApiResponseCode.TEAM_RECRUITMENT_NOT_FOUND;
import static in.koreatech.koin.global.code.ApiResponseCode.UNAUTHORIZED_USER;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import in.koreatech.koin.domain.teamrecruitment.dto.ChatMessageResponse;
import in.koreatech.koin.domain.teamrecruitment.dto.ChatRoomResponse;
import in.koreatech.koin.domain.teamrecruitment.dto.CreateChatMessageRequest;
import in.koreatech.koin.domain.teamrecruitment.dto.DirectChatRoomResponse;
import in.koreatech.koin.global.code.ApiResponseCodes;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "(Normal) Team Recruitment Chat: 팀원 모집 채팅", description = "팀원 모집 채팅 API")
public interface TeamRecruitmentChatApi {

    @ApiResponseCodes({
        OK,
        TEAM_RECRUITMENT_CHAT_NOT_FOUND,
        TEAM_RECRUITMENT_CHAT_FORBIDDEN,
        UNAUTHORIZED_USER,
        FORBIDDEN_USER_TYPE,
    })
    @Operation(summary = "팀 또는 개인 채팅방 정보 조회")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "채팅방 조회 성공"),
        @ApiResponse(responseCode = "403", description = "채팅방 멤버가 아님"),
        @ApiResponse(responseCode = "404", description = "채팅방을 찾을 수 없음")
    })
    ResponseEntity<ChatRoomResponse> getChatRoom(
            Integer userId,
            @PathVariable Integer recruitmentId,
            @PathVariable Integer chatRoomId
    );

    @ApiResponseCodes({
        OK,
        TEAM_RECRUITMENT_NOT_FOUND,
        TEAM_RECRUITMENT_APPLICATION_NOT_FOUND,
        TEAM_RECRUITMENT_FORBIDDEN,
        UNAUTHORIZED_USER,
        FORBIDDEN_USER_TYPE,
    })
    @Operation(summary = "지원자와 개인 채팅방 생성 또는 조회")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "기존 채팅방 반환"),
        @ApiResponse(responseCode = "201", description = "채팅방 신규 생성"),
        @ApiResponse(responseCode = "403", description = "모집글 작성자가 아님"),
        @ApiResponse(responseCode = "404", description = "지원서 또는 모집글을 찾을 수 없음"),
        @ApiResponse(responseCode = "409", description = "승인되지 않은 지원서 또는 마감된 모집")
    })
    ResponseEntity<DirectChatRoomResponse> getOrCreateDirectChatRoom(
            Integer userId,
            @PathVariable Integer recruitmentId,
            @PathVariable Integer applicationId
    );

    @ApiResponseCodes({
        OK,
        TEAM_RECRUITMENT_CHAT_FORBIDDEN,
        UNAUTHORIZED_USER,
        FORBIDDEN_USER_TYPE,
    })
    @Operation(summary = "채팅 메시지 조회 (Polling)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "메시지 조회 성공"),
        @ApiResponse(responseCode = "400", description = "afterMessageId와 beforeMessageId 동시 사용 불가, 또는 limit 범위 초과(1~200)"),
        @ApiResponse(responseCode = "403", description = "채팅방 멤버가 아님"),
        @ApiResponse(responseCode = "404", description = "채팅방을 찾을 수 없음")
    })
    ResponseEntity<List<ChatMessageResponse>> getMessages(
            Integer userId,
            @PathVariable Integer recruitmentId,
            @PathVariable Integer chatRoomId,
            @RequestParam(required = false) Integer afterMessageId,
            @RequestParam(required = false) Integer beforeMessageId,
            @RequestParam(defaultValue = "100") int limit
    );

    @ApiResponseCodes({
        OK,
        INVALID_REQUEST_BODY,
        TEAM_RECRUITMENT_CHAT_NOT_FOUND,
        TEAM_RECRUITMENT_CHAT_FORBIDDEN,
        TEAM_RECRUITMENT_CHAT_READ_ONLY,
        UNAUTHORIZED_USER,
        FORBIDDEN_USER_TYPE,
    })
    @Operation(summary = "메시지 전송")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "메시지 전송 성공"),
        @ApiResponse(responseCode = "403", description = "채팅방 멤버가 아님"),
        @ApiResponse(responseCode = "404", description = "채팅방을 찾을 수 없음"),
        @ApiResponse(responseCode = "409", description = "읽기 전용 채팅방")
    })
    ResponseEntity<ChatMessageResponse> createMessage(
            Integer userId,
            @PathVariable Integer recruitmentId,
            @PathVariable Integer chatRoomId,
            @RequestBody CreateChatMessageRequest request
    );
}
