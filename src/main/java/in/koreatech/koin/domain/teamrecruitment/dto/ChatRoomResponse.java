package in.koreatech.koin.domain.teamrecruitment.dto;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;

import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitmentChatRoom;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(SnakeCaseStrategy.class)
public record ChatRoomResponse(
        @Schema(description = "채팅방 ID", example = "31")
        Integer chatRoomId,

        @Schema(description = "채팅방 이름", example = "AI 아이디어 공모전 팀원 모집")
        String roomName,

        @Schema(description = "채팅방 타입", example = "TEAM")
        String roomType,

        @Schema(description = "채팅방 상태", example = "ACTIVE")
        String status,

        @Schema(
                description = "현재 채팅방 멤버 수. TEAM은 작성자와 승인된 지원자를 포함하고, DIRECT는 항상 2",
                example = "3"
        )
        int memberCount,

        @Schema(
                description = "최대 채팅방 멤버 수. TEAM은 모집 정원에 작성자 1명을 더한 값이고, DIRECT는 항상 2",
                example = "6"
        )
        int maxMemberCount,

        @Schema(description = "상대방 정보 (DIRECT만 non-null)")
        Counterpart counterpart
) {
    public record Counterpart(
            @Schema(description = "상대방 유저 ID", example = "22")
            Integer id,

            @Schema(description = "상대방 닉네임", example = "김철수")
            String nickname
    ) {}

    public static ChatRoomResponse of(
            TeamRecruitmentChatRoom chatRoom,
            String roomName,
            int memberCount,
            int maxMemberCount,
            Counterpart counterpart
    ) {
        return new ChatRoomResponse(
                chatRoom.getId(),
                roomName,
                chatRoom.getRoomType().name(),
                chatRoom.getStatus().name(),
                memberCount,
                maxMemberCount,
                counterpart
        );
    }
}
