package in.koreatech.koin.domain.club.recruitment.model;

import in.koreatech.koin.common.model.BaseEntity;
import in.koreatech.koin.domain.club.club.model.Club;
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
    name = "club_recruitment_subscription",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"club_id", "user_id"})
    }
)
@NoArgsConstructor(access = PROTECTED)
public class ClubRecruitmentSubscription extends BaseEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "club_id", nullable = false)
    private Club club;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Builder
    private ClubRecruitmentSubscription(
        Club club,
        User user
    ) {
        this.club = club;
        this.user = user;
    }
}
