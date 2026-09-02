package in.koreatech.koin.domain.teamrecruitment.dto;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitmentChatRoom;
import in.koreatech.koin.domain.user.model.User;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(SnakeCaseStrategy.class)
public record DirectChatRoomResponse(
        @Schema(description = "채팅방 ID", example = "71", requiredMode = REQUIRED)
        Integer chatRoomId,

        @Schema(description = "상대방 닉네임", example = "김철수", requiredMode = REQUIRED)
        String roomName,

        @Schema(description = "채팅방 타입", example = "DIRECT", requiredMode = REQUIRED)
        String roomType,

        @Schema(description = "채팅방 상태", example = "ACTIVE", requiredMode = REQUIRED)
        String status,

        @Schema(description = "상대방 정보", requiredMode = REQUIRED)
        Counterpart counterpart
) {
    public record Counterpart(
            @Schema(description = "상대방 유저 ID", example = "22", requiredMode = REQUIRED)
            Integer id,

            @Schema(description = "상대방 닉네임", example = "김철수", requiredMode = REQUIRED)
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
