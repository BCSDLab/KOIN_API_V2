package in.koreatech.koin.domain.club.model;

import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

import in.koreatech.koin._common.model.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = PROTECTED)
@Table(name = "club_event_image")
public class ClubEventImage extends BaseEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "club_event_id", nullable = false)
    private ClubEvent clubEvent;

    @Size(max = 255)
    @Column(name = "image_url", nullable = false)
    private String imageUrl;

    @Builder
    private ClubEventImage(ClubEvent clubEvent, String imageUrl) {
        this.clubEvent = clubEvent;
        this.imageUrl = imageUrl;
    }

    public void setClubEvent(ClubEvent clubEvent) {
        this.clubEvent = clubEvent;
    }
}
