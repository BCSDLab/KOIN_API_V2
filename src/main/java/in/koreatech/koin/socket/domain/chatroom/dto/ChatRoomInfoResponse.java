package in.koreatech.koin.socket.domain.chatroom.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(value = SnakeCaseStrategy.class)
public record ChatRoomInfoResponse(

    @Schema(description = "분실물 게시글 ID", example = "14465")
    Integer articleId,
    @Schema(description = "채팅방 ID", example = "1")
    Integer chatRoomId,
    @Schema(description = "사용자 ID", example = "5665")
    Integer userId,
    @Schema(description = "분실물 게시글 제목", example = "전자제품 | 담헌실학관 401호 | 25.01.15")
    String articleTitle,
    @Schema(description = "체팅 상대방 프로필 사진 url")
    String chatPartnerProfileImage
) {

    public static ChatRoomInfoResponse from(Integer articleId, Integer chatRoomId, Integer userId, String articleTitle, String chatPartnerProfileImage) {
        return new ChatRoomInfoResponse(articleId, chatRoomId, userId, articleTitle, chatPartnerProfileImage);
    }
}
