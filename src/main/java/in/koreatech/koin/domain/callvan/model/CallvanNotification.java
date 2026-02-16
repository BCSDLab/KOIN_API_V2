package in.koreatech.koin.domain.callvan.model;

import static lombok.AccessLevel.PROTECTED;

import java.time.LocalDate;
import java.time.LocalTime;

import org.hibernate.annotations.Where;

import in.koreatech.koin.common.model.BaseEntity;
import in.koreatech.koin.domain.callvan.model.enums.CallvanLocation;
import in.koreatech.koin.domain.callvan.model.enums.CallvanNotificationType;
import in.koreatech.koin.domain.user.model.User;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
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

@Getter
@Entity
@Table(name = "callvan_notification")
@Where(clause = "is_deleted=0")
@NoArgsConstructor(access = PROTECTED)
public class CallvanNotification extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id", nullable = false)
    private User recipient;

    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type", nullable = false, length = 30)
    private CallvanNotificationType notificationType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "callvan_post_id")
    private CallvanPost post;

    @Enumerated(EnumType.STRING)
    @Column(name = "departure_type", length = 20)
    private CallvanLocation departureType;

    @Column(name = "departure_custom_name", length = 50)
    private String departureCustomName;

    @Enumerated(EnumType.STRING)
    @Column(name = "arrival_type", length = 20)
    private CallvanLocation arrivalType;

    @Column(name = "arrival_custom_name", length = 50)
    private String arrivalCustomName;

    @Column(name = "departure_date")
    private LocalDate departureDate;

    @Column(name = "departure_time")
    @Convert(disableConversion = true)
    private LocalTime departureTime;

    @Column(name = "current_participants")
    private Integer currentParticipants;

    @Column(name = "max_participants")
    private Integer maxParticipants;

    @Column(name = "sender_nickname", length = 50)
    private String senderNickname;

    @Column(name = "message_preview", length = 100)
    private String messagePreview;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "callvan_chat_room_id")
    private CallvanChatRoom chatRoom;

    @Column(name = "joined_member_nickname", length = 50)
    private String joinedMemberNickname;

    @Column(name = "is_read", nullable = false)
    private Boolean isRead = false;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    @Builder
    private CallvanNotification(
            User recipient,
            CallvanNotificationType notificationType,
            CallvanPost post,
            CallvanLocation departureType,
            String departureCustomName,
            CallvanLocation arrivalType,
            String arrivalCustomName,
            LocalDate departureDate,
            LocalTime departureTime,
            Integer currentParticipants,
            Integer maxParticipants,
            String senderNickname,
            String messagePreview,
            CallvanChatRoom chatRoom,
            String joinedMemberNickname,
            Boolean isRead,
            Boolean isDeleted) {
        this.recipient = recipient;
        this.notificationType = notificationType;
        this.post = post;
        this.departureType = departureType;
        this.departureCustomName = departureCustomName;
        this.arrivalType = arrivalType;
        this.arrivalCustomName = arrivalCustomName;
        this.departureDate = departureDate;
        this.departureTime = departureTime;
        this.currentParticipants = currentParticipants;
        this.maxParticipants = maxParticipants;
        this.senderNickname = senderNickname;
        this.messagePreview = messagePreview;
        this.chatRoom = chatRoom;
        this.joinedMemberNickname = joinedMemberNickname;
        this.isRead = isRead != null ? isRead : false;
        this.isDeleted = isDeleted != null ? isDeleted : false;
    }
}
