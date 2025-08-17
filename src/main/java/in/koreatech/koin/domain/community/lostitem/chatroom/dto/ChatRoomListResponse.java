package in.koreatech.koin.domain.community.lostitem.chatroom.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@JsonNaming(value = SnakeCaseStrategy.class)
public record ChatRoomListResponse(
    @Schema(description = "분실물 게시글 제목", example = "전자제품 | 담헌실학관 401호 | 25.01.15")
    String articleTitle,

    @Schema(description = "마지막 전송 메시지", example = "안녕하세요?")
    String recentMessageContent,

    @Schema(description = "분실물 이미지 url (대표 1개)", example = "https://stage-static.koreatech.in/upload/LOST_ITEMS/2025/1/15/41d0953a-7ac8-46d5-87e4-f3277d3a9dc8/1000000059.jpg")
    String lostItemImageUrl,

    @Schema(description = "사용자가 읽지 않은 메시지 개수", example = "1")
    Integer unreadMessageCount,

    @Schema(description = "마지막 전송 메시지 전송 시간", example = "2025-01-18T20:08:10.6441253")
    LocalDateTime lastMessageAt,

    @Schema(description = "분실물 게시글 ID", example = "16641")
    Integer articleId,

    @Schema(description = "체팅방 ID", example = "1")
    Integer chatRoomId
) {

}
