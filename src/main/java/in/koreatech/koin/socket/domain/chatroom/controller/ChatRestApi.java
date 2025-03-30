package in.koreatech.koin.socket.domain.chatroom.controller;

import static in.koreatech.koin.domain.user.model.UserType.*;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import in.koreatech.koin._common.auth.Auth;
import in.koreatech.koin.socket.domain.chatroom.dto.ChatRoomInfoResponse;
import in.koreatech.koin.socket.domain.chatroom.dto.ChatRoomListResponse;
import in.koreatech.koin.socket.domain.message.dto.ChatMessageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "(Normal) LostItemChat: 분실물 쪽지", description = "분실물 쪽지 채팅 관리")
@RequestMapping("/chatroom")
public interface ChatRestApi {

    @ApiResponses(
        value = {
            @ApiResponse(
                responseCode = "201",
                description = "채팅방 생성 성공",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ChatRoomInfoResponse.class, example = """
                    {
                        "articleId": 14465,
                        "chatRoomId": 1,
                        "userId": 5665,
                        "articleTitle": "전자제품 | 담헌실학관 401호 | 25.01.15",
                        "chatPartnerProfileImage": "http://example.com/profile.jpg"
                    }
                """)
                )
            ),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "422", content = @Content(schema = @Schema(hidden = true))),
        }
    )
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
        @Auth(permit= {GENERAL, STUDENT, COUNCIL}) Integer studentId,
        @PathVariable("articleId") Integer articleId
    );

    @ApiResponses(
        value = {
            @ApiResponse(
                responseCode = "201",
                description = "사용자 차단 성공"
            ),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "422", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(
        summary = "채팅방 에서 사용자 차단",
        description = """
            ### articleId를 이용하여 사용자를 차단
            - 서버에서 글쓴이의 ID를 조회하여 차단합니다.
            """
    )
    @PostMapping("/lost-item/{articleId}/{chatRoomId}/block")
    ResponseEntity<?> blockChatUser(
        @Auth(permit= {GENERAL, STUDENT, COUNCIL}) Integer studentId,
        @PathVariable("articleId") Integer articleId,
        @PathVariable("chatRoomId") Integer chatRoomId
    );

    @ApiResponses(
        value = {
            @ApiResponse(
                responseCode = "200",
                description = "사용자 차단 해제 성공"
            ),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "422", content = @Content(schema = @Schema(hidden = true))),
        }
    )
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

    @ApiResponses(
        value = {
            @ApiResponse(
                responseCode = "200",
                description = "채팅방 정보 반환 성공",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ChatRoomInfoResponse.class, example = """
                    {
                        "articleId": 14465,
                        "chatRoomId": 1,
                        "userId": 5665,
                        "articleTitle": "전자제품 | 담헌실학관 401호 | 25.01.15",
                        "chatPartnerProfileImage": "http://example.com/profile.jpg"
                    }
                """)
                )
            ),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "422", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(
        summary = "채팅방 목록(쪽지 리스트 페이지)에서 채팅방 진입시 필요한 데이터 반환",
        description = """
            ### 분실물 게시글 채팅방 데이터 반환
            - 채팅방 목록(쪽지 리스트 페이지) 에서 채팅방에 진입할 때 가장 먼저 호출 해야 하는 api 엔드포인트 입니다.
            """
    )
    @GetMapping("/lost-item/{articleId}/{chatRoomId}")
    ResponseEntity<?> getLostItemChatRoom(
        @Auth(permit= {GENERAL, STUDENT, COUNCIL}) Integer studentId,
        @PathVariable("articleId") Integer articleId,
        @PathVariable("chatRoomId") Integer chatRoomId
    );


    @Operation(
        summary = "채팅방의 모든 메시지 반환",
        description = """
            ### 채팅방의 모든 메시지를 반환합니다.
            - 클라이언트가 채팅방에 접속할 때 이전 메시지를 출력하기 위해 호출해야 하는 api 엔드포인트 입니다.
            """
    )
    @GetMapping("/lost-item/{articleId}/{chatRoomId}/messages")
    ResponseEntity<List<ChatMessageResponse>> getAllMessages(
        @Auth(permit= {GENERAL, STUDENT, COUNCIL}) Integer studentId,
        @PathVariable("articleId") Integer articleId,
        @PathVariable("chatRoomId") Integer chatRoomId
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "422", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(
        summary = "채팅방 목록(쪽지 리스트) 데이터 반환",
        description = """
            ### 채팅방 목록 데이터 반환
            - 채팅방 목록(쪽지 리스트 페이지) 데이터를 반환합니다.
            """
    )
    @GetMapping("/lost-item/")
    ResponseEntity<List<ChatRoomListResponse>> getAllChatRoomInfo(
        @Auth(permit= {GENERAL, STUDENT, COUNCIL}) Integer studentId
    );
}
