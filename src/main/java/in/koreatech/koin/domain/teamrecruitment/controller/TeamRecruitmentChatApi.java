package in.koreatech.koin.domain.teamrecruitment.controller;

import static in.koreatech.koin.global.code.ApiResponseCode.CREATED;
import static in.koreatech.koin.global.code.ApiResponseCode.FORBIDDEN_USER_TYPE;
import static in.koreatech.koin.global.code.ApiResponseCode.ILLEGAL_ARGUMENT;
import static in.koreatech.koin.global.code.ApiResponseCode.INVALID_REQUEST_BODY;
import static in.koreatech.koin.global.code.ApiResponseCode.OK;
import static in.koreatech.koin.global.code.ApiResponseCode.TEAM_RECRUITMENT_APPLICATION_NOT_ACCEPTED;
import static in.koreatech.koin.global.code.ApiResponseCode.TEAM_RECRUITMENT_APPLICATION_NOT_FOUND;
import static in.koreatech.koin.global.code.ApiResponseCode.TEAM_RECRUITMENT_CHAT_FORBIDDEN;
import static in.koreatech.koin.global.code.ApiResponseCode.TEAM_RECRUITMENT_CHAT_NOT_FOUND;
import static in.koreatech.koin.global.code.ApiResponseCode.TEAM_RECRUITMENT_CHAT_READ_ONLY;
import static in.koreatech.koin.global.code.ApiResponseCode.TEAM_RECRUITMENT_CLOSED;
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
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
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
    ResponseEntity<ChatRoomResponse> getChatRoom(
            Integer userId,
            @PathVariable Integer recruitmentId,
            @PathVariable Integer chatRoomId
    );

    @ApiResponseCodes({
        OK,
        CREATED,
        TEAM_RECRUITMENT_NOT_FOUND,
        TEAM_RECRUITMENT_APPLICATION_NOT_FOUND,
        TEAM_RECRUITMENT_FORBIDDEN,
        TEAM_RECRUITMENT_APPLICATION_NOT_ACCEPTED,
        TEAM_RECRUITMENT_CLOSED,
        UNAUTHORIZED_USER,
        FORBIDDEN_USER_TYPE,
    })
    @Operation(
        summary = "지원자와 개인 채팅방 생성 또는 조회",
        description = "ACCEPTED 지원서만 대상입니다. 기존 DIRECT 채팅방이 있으면 모집 상태와 관계없이 기존 방을 반환합니다. "
            + "기존 방이 없을 때는 마감일이 지나지 않은 RECRUITING 상태이거나 "
            + "정원 충족으로 마감되어 ACTIVE 상태인 TEAM 채팅방이 있는 경우에만 새 방을 생성합니다."
    )
    ResponseEntity<DirectChatRoomResponse> getOrCreateDirectChatRoom(
            Integer userId,
            @PathVariable Integer recruitmentId,
            @PathVariable Integer applicationId
    );

    @ApiResponseCodes({
        OK,
        ILLEGAL_ARGUMENT,
        TEAM_RECRUITMENT_CHAT_NOT_FOUND,
        TEAM_RECRUITMENT_CHAT_FORBIDDEN,
        UNAUTHORIZED_USER,
        FORBIDDEN_USER_TYPE,
    })
    @Operation(summary = "채팅 메시지 조회 (Polling)")
    ResponseEntity<List<ChatMessageResponse>> getMessages(
            Integer userId,
            @PathVariable Integer recruitmentId,
            @PathVariable Integer chatRoomId,
            @Parameter(schema = @Schema(minimum = "1"))
            @RequestParam(name = "afterMessageId", required = false) Integer afterMessageId,
            @Parameter(schema = @Schema(minimum = "1"))
            @RequestParam(name = "beforeMessageId", required = false) Integer beforeMessageId,
            @Parameter(schema = @Schema(minimum = "1", maximum = "200"))
            @RequestParam(name = "limit", defaultValue = "100") int limit
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
    ResponseEntity<ChatMessageResponse> createMessage(
            Integer userId,
            @PathVariable Integer recruitmentId,
            @PathVariable Integer chatRoomId,
            @RequestBody CreateChatMessageRequest request
    );
}
