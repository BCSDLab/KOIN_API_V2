package in.koreatech.koin.domain.community.lostitem.chatroom.controller;

import static in.koreatech.koin.domain.user.model.UserType.*;
import static in.koreatech.koin.domain.user.model.UserType.COUNCIL;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import in.koreatech.koin.global.auth.Auth;
import in.koreatech.koin.global.code.ApiResponseCode;
import in.koreatech.koin.global.code.ApiResponseCodes;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "(Normal) LostItemChatUserBlock: 분실물 쪽지 사용자 차단", description = "분실물 쪽지 채팅 사용자 차단 관리")
@RequestMapping("/chatroom")
public interface LostItemChatRoomUserBlockApi {


    @ApiResponseCodes({
        ApiResponseCode.NO_CONTENT
    })
    @Operation(
        summary = "채팅방 에서 사용자 차단",
        description = """
            ### articleId를 이용하여 사용자를 차단
            - 서버에서 글쓴이의 ID를 조회하여 차단합니다.
            """
    )
    @PostMapping("/lost-item/{articleId}/{chatRoomId}/block")
    ResponseEntity<?> blockChatUser(
        @Auth(permit= {GENERAL, STUDENT, COUNCIL}) Integer userId,
        @PathVariable("articleId") Integer articleId,
        @PathVariable("chatRoomId") Integer chatRoomId
    );

    @ApiResponseCodes({
        ApiResponseCode.NO_CONTENT
    })
    @Operation(
        summary = "채팅방에서 사용자 차단 해제(개발 편의용)",
        description = """
        ### articleId를 이용하여 사용자 차단을 해제
        - 서버에서 글쓴이의 ID를 조회하여 차단을 해제합니다.
        """
    )
    @DeleteMapping("/lost-item/{articleId}/{chatRoomId}/unblock")
    ResponseEntity<?> unblockChatUser(
        @Auth(permit= {GENERAL, STUDENT, COUNCIL}) Integer studentId,
        @PathVariable("articleId") Integer articleId,
        @PathVariable("chatRoomId") Integer chatRoomId
    );
}
