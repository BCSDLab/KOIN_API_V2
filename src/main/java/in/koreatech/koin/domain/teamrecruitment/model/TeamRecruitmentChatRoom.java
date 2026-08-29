package in.koreatech.koin.domain.teamrecruitment.model;

import static lombok.AccessLevel.PROTECTED;

import in.koreatech.koin.common.model.BaseEntity;
import in.koreatech.koin.domain.teamrecruitment.model.enums.ChatRoomStatus;
import in.koreatech.koin.domain.teamrecruitment.model.enums.ChatRoomType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "team_recruitment_chat_room")
@NoArgsConstructor(access = PROTECTED)
public class TeamRecruitmentChatRoom extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "recruitment_id", nullable = false)
    private Integer recruitmentId;

    @Column(name = "room_name", nullable = false, length = 100)
    private String roomName;

    @Enumerated(EnumType.STRING)
    @Column(name = "room_type", nullable = false, length = 10)
    private ChatRoomType roomType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 15)
    private ChatRoomStatus status;

    @Column(name = "max_member_count", nullable = false)
    private Integer maxMemberCount;

    public void close() {
        this.status = ChatRoomStatus.READ_ONLY;
    }

    @Builder
    private TeamRecruitmentChatRoom(
            Integer recruitmentId,
            String roomName,
            ChatRoomType roomType,
            Integer maxMemberCount) {
        this.recruitmentId = recruitmentId;
        this.roomName = roomName;
        this.roomType = roomType;
        this.status = ChatRoomStatus.ACTIVE;
        this.maxMemberCount = maxMemberCount;
    }
}
