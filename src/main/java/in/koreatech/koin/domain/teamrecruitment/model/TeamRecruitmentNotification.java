package in.koreatech.koin.domain.teamrecruitment.model;

import static lombok.AccessLevel.PROTECTED;

import in.koreatech.koin.common.model.BaseEntity;
import in.koreatech.koin.domain.teamrecruitment.model.enums.TeamRecruitmentNotificationType;
import in.koreatech.koin.domain.user.model.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Where;

@Getter
@Entity
@Table(name = "team_recruitment_notification")
@Where(clause = "is_deleted=0")
@NoArgsConstructor(access = PROTECTED)
public class TeamRecruitmentNotification extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id", nullable = false)
    private User recipient;

    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type", nullable = false, length = 30)
    private TeamRecruitmentNotificationType notificationType;

    @Column(name = "recruitment_id")
    private Integer recruitmentId;

    @Column(name = "application_id")
    private Integer applicationId;

    @Column(name = "sender_nickname", length = 50)
    private String senderNickname;

    @Column(name = "chat_room_id")
    private Integer chatRoomId;

    @Column(name = "message_preview", length = 100)
    private String messagePreview;

    @Column(name = "is_read", nullable = false)
    private Boolean isRead = false;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    @Builder
    private TeamRecruitmentNotification(
            User recipient,
            TeamRecruitmentNotificationType notificationType,
            Integer recruitmentId,
            Integer applicationId,
            Integer chatRoomId,
            String senderNickname,
            String messagePreview) {
        this.recipient = recipient;
        this.notificationType = notificationType;
        this.recruitmentId = recruitmentId;
        this.applicationId = applicationId;
        this.chatRoomId = chatRoomId;
        this.senderNickname = senderNickname;
        this.messagePreview = messagePreview;
        this.isRead = false;
        this.isDeleted = false;
    }
}
