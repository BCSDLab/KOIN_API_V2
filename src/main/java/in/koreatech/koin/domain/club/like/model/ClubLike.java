package in.koreatech.koin.domain.club.like.model;

import in.koreatech.koin.common.model.BaseEntity;
import in.koreatech.koin.domain.club.club.model.Club;
import in.koreatech.koin.domain.user.model.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

@Getter
@Entity
@Table(name = "club_like")
@NoArgsConstructor(access = PROTECTED)
public class ClubLike extends BaseEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Integer id;

    @JoinColumn(name = "club_id")
    @ManyToOne(fetch = LAZY)
    private Club club;

    @JoinColumn(name = "user_id")
    @ManyToOne(fetch = LAZY)
    private User user;

    @Builder
    private ClubLike(
        Integer id,
        Club club,
        User user
    ) {
        this.id = id;
        this.club = club;
        this.user = user;
    }
}
