package in.koreatech.koin.domain.team.recruitment.model;

import in.koreatech.koin.common.model.BaseEntity;
import in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentNotificationTargetType;
import in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentNotificationType;
import in.koreatech.koin.domain.user.model.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

@Getter
@Entity
@Table(name = "team_recruitment_notification")
@NoArgsConstructor(access = PROTECTED)
public class TeamRecruitmentNotification extends BaseEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recipient_id", nullable = false, updatable = false)
    private User recipient;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 32)
    private TeamRecruitmentNotificationType type;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false, length = 32)
    private TeamRecruitmentNotificationTargetType targetType;

    @NotNull
    @Size(max = 255)
    @Column(name = "message_preview", nullable = false, length = 255)
    private String messagePreview;

    @Size(max = 50)
    @Column(name = "sender_nickname", length = 50, updatable = false)
    private String senderNickname;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recruitment_id", nullable = false, updatable = false)
    private TeamRecruitment recruitment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id", updatable = false)
    private TeamRecruitmentApplication application;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id", updatable = false)
    private TeamRecruitmentChatRoom chatRoom;

    @Column(name = "read_at", columnDefinition = "TIMESTAMP")
    private LocalDateTime readAt;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    @Builder
    private TeamRecruitmentNotification(
        Integer id,
        User recipient,
        TeamRecruitmentNotificationType type,
        TeamRecruitmentNotificationTargetType targetType,
        String messagePreview,
        String senderNickname,
        TeamRecruitment recruitment,
        TeamRecruitmentApplication application,
        TeamRecruitmentChatRoom chatRoom,
        LocalDateTime readAt,
        Boolean isDeleted
    ) {
        this.id = id;
        this.recipient = recipient;
        this.type = type;
        this.targetType = targetType;
        this.messagePreview = messagePreview;
        this.senderNickname = senderNickname;
        this.recruitment = recruitment;
        this.application = application;
        this.chatRoom = chatRoom;
        this.readAt = readAt;
        this.isDeleted = Boolean.TRUE.equals(isDeleted);
    }

    public void markAsRead(LocalDateTime now) {
        if (readAt == null) {
            readAt = now;
        }
    }

    public void delete() {
        isDeleted = true;
    }

    @Transient
    public boolean isRead() {
        return readAt != null;
    }
}
