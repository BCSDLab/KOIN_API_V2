package in.koreatech.koin.domain.callvan.model;

import static lombok.AccessLevel.PROTECTED;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.Where;

import in.koreatech.koin.common.model.BaseEntity;
import in.koreatech.koin.domain.callvan.model.enums.CallvanLocation;
import in.koreatech.koin.domain.callvan.model.enums.CallvanStatus;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "callvan_post")
@Where(clause = "is_deleted=0")
@NoArgsConstructor(access = PROTECTED)
public class CallvanPost extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @Column(name = "title", nullable = false, length = 100)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(name = "departure_type", nullable = false, length = 20)
    private CallvanLocation departureType;

    @Column(name = "departure_custom_name", length = 50)
    private String departureCustomName;

    @Enumerated(EnumType.STRING)
    @Column(name = "arrival_type", nullable = false, length = 20)
    private CallvanLocation arrivalType;

    @Column(name = "arrival_custom_name", length = 50)
    private String arrivalCustomName;

    @Column(name = "departure_date", nullable = false)
    private LocalDate departureDate;

    @Column(name = "departure_time", nullable = false)
    @Convert(disableConversion = true)
    private LocalTime departureTime;

    @Column(name = "max_participants", nullable = false)
    private Integer maxParticipants;

    @Column(name = "current_participants", nullable = false)
    private Integer currentParticipants = 1;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private CallvanStatus status = CallvanStatus.RECRUITING;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id")
    private CallvanChatRoom chatRoom;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    @OneToMany(mappedBy = "post", fetch = FetchType.LAZY)
    private List<CallvanParticipant> participants = new ArrayList<>();

    @Builder
    private CallvanPost(
            User author,
            String title,
            CallvanLocation departureType,
            String departureCustomName,
            CallvanLocation arrivalType,
            String arrivalCustomName,
            LocalDate departureDate,
            LocalTime departureTime,
            Integer maxParticipants,
            Integer currentParticipants,
            CallvanStatus status,
            Boolean isDeleted) {
        this.author = author;
        this.title = title;
        this.departureType = departureType;
        this.departureCustomName = departureCustomName;
        this.arrivalType = arrivalType;
        this.arrivalCustomName = arrivalCustomName;
        this.departureDate = departureDate;
        this.departureTime = departureTime;
        this.maxParticipants = maxParticipants;
        this.currentParticipants = currentParticipants != null ? currentParticipants : 1;
        this.status = status != null ? status : CallvanStatus.RECRUITING;
        this.isDeleted = isDeleted != null ? isDeleted : false;
    }

    public void updateChatRoom(CallvanChatRoom chatRoom) {
        this.chatRoom = chatRoom;
    }
}
