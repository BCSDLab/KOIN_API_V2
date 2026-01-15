package in.koreatech.koin.domain.community.lostitem.chatroom.controller;

import static in.koreatech.koin.domain.user.model.UserType.*;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import in.koreatech.koin.global.auth.Auth;
import in.koreatech.koin.domain.community.lostitem.chatroom.dto.ChatRoomInfoResponse;
import in.koreatech.koin.domain.community.lostitem.chatroom.dto.ChatRoomListResponse;
import in.koreatech.koin.domain.community.lostitem.chatmessage.dto.ChatMessageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "(Normal) LostItemChat: 분실물 쪽지", description = "분실물 쪽지 채팅 관리")
@RequestMapping("/chatroom")
public interface LostItemChatRoomApi {

    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "채팅방 생성 성공",
            content = @Content(mediaType = "application/json", examples = {
                @ExampleObject(name = "생성 성공", value = """
                    {
                      "article_id": 1234,
                      "chat_room_id": 1,
                      "user_id": 10,
                      "article_title": "신분증 | 학생회관 앞 | 25.01.01",
                      "chat_partner_profile_image": null
                    }
                    """)
            })
        ),
        @ApiResponse(responseCode = "403", description = "인증 정보 오류",
            content = @Content(mediaType = "application/json", examples = {
                @ExampleObject(name = "차단된 사용자", summary = "차단된 사용자", value = """
                    {
                      "message": "차단된 사용자입니다.",
                      "errorTraceId": "123e4567-e89b-12d3-a456-426614174000",
                      "code": "FORBIDDEN_BLOCKED_USER"
                    }
                    """)
            })
        ),
        @ApiResponse(responseCode = "400", description = "잘못된 요청",
            content = @Content(mediaType = "application/json", examples = {
                @ExampleObject(name = "자신이 올린 게시글에 채팅 시도", summary = "자신이 올린 게시글에 메시지를 보낼 수 없습니다", value = """
                    {
                      "message": "자신이 올린 게시글에 메시지를 보낼 수 없습니다.",
                      "errorTraceId": "123e4567-e89b-12d3-a456-426614174000",
                      "code": "INVALID_SELF_CHAT"
                    }
                    """)
            })
        ),
        @ApiResponse(responseCode = "404", description = "리소스 없음",
            content = @Content(mediaType = "application/json", examples = {
                @ExampleObject(name = "채팅 상대방 탈퇴", summary = "채팅 상대방이 존재하지 않습니다.", value = """
                    {
                      "message": "채팅 상대방이 존재하지 않습니다.",
                      "errorTraceId": "123e4567-e89b-12d3-a456-426614174000",
                      "code": "NOT_FOUND_CHAT_PARTNER"
                    }
                    """)
            })
        )
    })
    @Operation(
        summary = "분실물 게시글에서 직접 채팅방 진입시 채팅방 데이터 생성, 필요한 데이터 반환",
        description = """
            ### 분실물 게시글 채팅방 생성, 관련 데이터 반환
            - 분실물 게시글의 '쪽지 보내기' 버튼을 클릭했을 때 가장 먼저 호출해야 하는 api 엔드포인트 입니다.
            - 사용자가 처음 채팅을 보내는 것이라면 chatRoomId를 생성하여 반환합니다.
            - 이미 대화가 진행된 채팅방이라면 chatRoomId를 탐색해서 반환합니다.
            """
    )
    @PostMapping("/lost-item/{articleId}")
    ResponseEntity<ChatRoomInfoResponse> createLostItemChatRoom(
        @Auth(permit= {GENERAL, STUDENT, COUNCIL}) Integer userId,
        @PathVariable("articleId") Integer articleId
    );

    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "채팅방 조회 성공",
            content = @Content(mediaType = "application/json", examples = {
                @ExampleObject(name = "생성 성공", value = """
                    {
                      "article_id": 1234,
                      "chat_room_id": 1,
                      "user_id": 10,
                      "article_title": "신분증 | 학생회관 앞 | 25.01.01",
                      "chat_partner_profile_image": null
                    }
                    """)
            })
        ),
        @ApiResponse(responseCode = "403", description = "인증 정보 오류",
            content = @Content(mediaType = "application/json", examples = {
                @ExampleObject(name = "차단된 사용자", summary = "차단된 사용자", value = """
                    {
                      "message": "차단된 사용자입니다.",
                      "errorTraceId": "123e4567-e89b-12d3-a456-426614174000",
                      "code": "FORBIDDEN_BLOCKED_USER"
                    }
                    """)
            })
        ),
        @ApiResponse(responseCode = "404", description = "리소스 없음",
            content = @Content(mediaType = "application/json", examples = {
                @ExampleObject(name = "채팅방 미존재", summary = "분실물 게시글 채팅방이 존재하지 않습니다.", value = """
                    {
                      "message": "분실물 게시글 채팅방이 존재하지 않습니다.",
                      "errorTraceId": "123e4567-e89b-12d3-a456-426614174000",
                      "code": "NOT_FOUND_LOST_ITEM_CHATROOM"
                    }
                    """),
                @ExampleObject(name = "채팅 상대방 탈퇴", summary = "채팅 상대방이 존재하지 않습니다.", value = """
                    {
                      "message": "채팅 상대방이 존재하지 않습니다.",
                      "errorTraceId": "123e4567-e89b-12d3-a456-426614174000",
                      "code": "NOT_FOUND_CHAT_PARTNER"
                    }
                    """)
            })
        ),
    })
    @Operation(
        summary = "채팅방 목록(쪽지 리스트 페이지)에서 채팅방 진입시 필요한 데이터 반환",
        description = """
            ### 분실물 게시글 채팅방 데이터 반환
            - 채팅방 목록(쪽지 리스트 페이지) 에서 채팅방에 진입할 때 가장 먼저 호출 해야 하는 api 엔드포인트 입니다.
            """
    )
    @GetMapping("/lost-item/{articleId}/{chatRoomId}")
    ResponseEntity<ChatRoomInfoResponse> getLostItemChatRoom(
        @Auth(permit= {GENERAL, STUDENT, COUNCIL}) Integer userId,
        @PathVariable("articleId") Integer articleId,
        @PathVariable("chatRoomId") Integer chatRoomId
    );

    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "채팅 메시지 조회 성공",
            content = @Content(mediaType = "application/json", examples = {
                @ExampleObject(name = "조회 성공", value = """
                    [
                      {
                        "user_id": 1234,
                        "user_nickname": "홍길동",
                        "content": "안녕하세요",
                        "timestamp": "2025-07-16T20:46:09.8782654",
                        "is_image": false
                      }
                    ]
                    """)
            })
        )
    })
    @Operation(
        summary = "채팅방의 모든 메시지 반환",
        description = """
            ### 채팅방의 모든 메시지를 반환합니다.
            - 클라이언트가 채팅방에 접속할 때 이전 메시지를 출력하기 위해 호출해야 하는 api 엔드포인트 입니다.
            """
    )
    @GetMapping("/lost-item/{articleId}/{chatRoomId}/messages")
    ResponseEntity<List<ChatMessageResponse>> getAllMessages(
        @Auth(permit= {GENERAL, STUDENT, COUNCIL}) Integer userId,
        @PathVariable("articleId") Integer articleId,
        @PathVariable("chatRoomId") Integer chatRoomId
    );

    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "채팅방 목록 조회 성공",
            content = @Content(mediaType = "application/json", examples = {
                @ExampleObject(name = "조회 성공", value = """
                    [
                      {
                        "article_title": "신분증 | 학생회관 앞 | 25.01.01",
                        "recent_message_content": "안녕하세요",
                        "lost_item_image_url": "https://stage-static.koreatech.in/upload/LOST_ITEMS/example.gif",
                        "unread_message_count": 0,
                        "last_message_at": "2025-07-16T22:53:24.6379667",
                        "article_id": 1234,
                        "chat_room_id": 1
                      }
                    ]
                    """)
            })
        )
    })
    @Operation(
        summary = "채팅방 목록(쪽지 리스트) 데이터 반환",
        description = """
            ### 채팅방 목록 데이터 반환
            - 채팅방 목록(쪽지 리스트 페이지) 데이터를 반환합니다.
            """
    )
    @GetMapping("/lost-item")
    ResponseEntity<List<ChatRoomListResponse>> getAllChatRoomInfo(
        @Auth(permit= {GENERAL, STUDENT, COUNCIL}) Integer userId
    );
}
