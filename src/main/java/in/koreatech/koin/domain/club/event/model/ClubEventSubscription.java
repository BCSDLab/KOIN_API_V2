package in.koreatech.koin.domain.club.event.model;

import in.koreatech.koin.common.model.BaseEntity;
import in.koreatech.koin.domain.user.model.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

@Getter
@Entity
@Table(
    name = "club_event_subscription",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"event_id", "user_id"})
    }
)
@NoArgsConstructor(access = PROTECTED)
public class ClubEventSubscription extends BaseEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "event_id", nullable = false)
    private ClubEvent clubEvent;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Builder
    private ClubEventSubscription(
        ClubEvent clubEvent,
        User user
    ) {
        this.clubEvent = clubEvent;
        this.user = user;
    }
}
