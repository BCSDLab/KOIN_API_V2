package in.koreatech.koin.domain.teamrecruitment.dto;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.time.LocalDateTime;

import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentChatRoomType;
import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitmentChatMessage;
import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitmentChatRoom;
import in.koreatech.koin.domain.user.model.User;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(SnakeCaseStrategy.class)
public record TeamRecruitmentChatRoomListItemResponse(
    @Schema(description = "모집글 ID", example = "17", requiredMode = REQUIRED)
    Integer recruitmentId,

    @Schema(description = "채팅방 ID", example = "31", requiredMode = REQUIRED)
    Integer chatRoomId,

    @Schema(description = "채팅방 이름. TEAM은 모집글 제목, DIRECT는 상대방 닉네임입니다.",
        example = "AI 아이디어 공모전 팀원 모집", requiredMode = REQUIRED)
    String roomName,

    @Schema(description = "채팅방 타입", example = "TEAM", requiredMode = REQUIRED)
    String roomType,

    @Schema(description = "채팅방 상태", example = "ACTIVE", requiredMode = REQUIRED)
    String status,

    @Schema(description = "DIRECT 상대방 유저 ID. TEAM은 null입니다.", example = "22",
        nullable = true, requiredMode = REQUIRED)
    Integer counterpartId,

    @Schema(description = "DIRECT 상대방 닉네임. TEAM은 null입니다.", example = "김철수",
        nullable = true, requiredMode = REQUIRED)
    String counterpartNickname,

    @Schema(description = "최근 메시지 ID. 메시지가 없으면 null입니다.", example = "901",
        nullable = true, requiredMode = REQUIRED)
    Integer lastMessageId,

    @Schema(description = "최근 메시지 내용. 메시지가 없으면 null입니다.", example = "안녕하세요!",
        nullable = true, requiredMode = REQUIRED)
    String lastMessageContent,

    @Schema(description = "최근 메시지 전송 시각. 메시지가 없으면 null입니다.",
        example = "2026-08-26T11:20:30.123456", nullable = true, requiredMode = REQUIRED)
    LocalDateTime lastMessageAt,

    @Schema(description = "최근 메시지의 이미지 여부. 메시지가 없으면 null입니다.", example = "false",
        nullable = true, requiredMode = REQUIRED)
    Boolean lastMessageIsImage,

    @Schema(description = "현재 사용자가 읽지 않은 메시지 수", example = "2", requiredMode = REQUIRED)
    int unreadMessageCount
) {

    public static TeamRecruitmentChatRoomListItemResponse of(
        TeamRecruitmentChatRoom chatRoom,
        User counterpart,
        TeamRecruitmentChatMessage lastMessage,
        int unreadMessageCount
    ) {
        boolean direct = chatRoom.getRoomType() == TeamRecruitmentChatRoomType.DIRECT;
        return new TeamRecruitmentChatRoomListItemResponse(
            chatRoom.getRecruitment().getId(),
            chatRoom.getId(),
            direct && counterpart != null ? counterpart.getNickname() : chatRoom.getRecruitment().getTitle(),
            chatRoom.getRoomType().name(),
            chatRoom.getStatus().name(),
            direct && counterpart != null ? counterpart.getId() : null,
            direct && counterpart != null ? counterpart.getNickname() : null,
            lastMessage == null ? null : lastMessage.getId(),
            lastMessage == null ? null : lastMessage.getContent(),
            lastMessage == null ? null : lastMessage.getCreatedAt(),
            lastMessage == null ? null : lastMessage.getIsImage(),
            unreadMessageCount
        );
    }
}
