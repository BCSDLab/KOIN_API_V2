package in.koreatech.koin.domain.teamrecruitment.dto;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;

import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.teamrecruitment.model.TeamRecruitmentChatRoom;
import in.koreatech.koin.domain.user.model.User;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(SnakeCaseStrategy.class)
public record DirectChatRoomResponse(
        @Schema(description = "채팅방 ID", example = "71")
        Integer chatRoomId,

        @Schema(description = "상대방 닉네임", example = "김철수")
        String roomName,

        @Schema(description = "채팅방 타입", example = "DIRECT")
        String roomType,

        @Schema(description = "채팅방 상태", example = "ACTIVE")
        String status,

        @Schema(description = "상대방 정보")
        Counterpart counterpart
) {
    public record Counterpart(
            @Schema(description = "상대방 유저 ID", example = "22")
            Integer id,

            @Schema(description = "상대방 닉네임", example = "김철수")
            String nickname
    ) {}

    public static DirectChatRoomResponse of(TeamRecruitmentChatRoom chatRoom, User counterpartUser) {
        return new DirectChatRoomResponse(
                chatRoom.getId(),
                counterpartUser.getNickname(),
                chatRoom.getRoomType().name(),
                chatRoom.getStatus().name(),
                new Counterpart(counterpartUser.getId(), counterpartUser.getNickname())
        );
    }
}
