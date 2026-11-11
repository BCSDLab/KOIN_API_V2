package in.koreatech.koin.domain.chat.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.chat.model.ChatDomainType;
import in.koreatech.koin.domain.chat.model.ChatRoomType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@JsonNaming(SnakeCaseStrategy.class)
@Schema(name = "CommonChatRoomResponse")
public record ChatRoomResponse(
    @Schema(description = "공통 채팅방 식별자. 클라이언트는 값을 분해하지 않고 그대로 사용합니다.",
        example = "lost-item-14465-1")
    String roomId,
    ChatDomainType domainType,
    @Schema(description = "연결된 게시글 ID. 기존 게시글 이동과 API 호출에 사용합니다.")
    Integer contextId,
    @Schema(description = "기존 도메인의 채팅방 ID")
    Integer chatRoomId,
    ChatRoomType roomType,
    @Schema(description = "현재 사용자의 메시지 전송 가능 여부")
    Boolean canSend,
    @Schema(description = "메시지 읽음 확인 기능 지원 여부")
    Boolean supportsReadReceipts,
    String roomName,
    String imageUrl,
    Integer memberCount,
    Integer maxMemberCount,
    Integer counterpartId,
    String counterpartNickname,
    String counterpartProfileImage,
    @Schema(description = "현재 사용자가 읽지 않은 다른 참여자의 메시지 수")
    Integer unreadMessageCount,
    String lastMessageId,
    Boolean lastMessageIsImage,
    String lastMessageContent,
    LocalDateTime lastMessageAt
) {
}
