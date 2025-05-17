package in.koreatech.koin.domain.club.model;

import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

import in.koreatech.koin.domain.user.model.User;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "club_manager")
@NoArgsConstructor(access = PROTECTED)
public class ClubManager {

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
    private ClubManager(
        Integer id,
        Club club,
        User user
    ) {
        this.id = id;
        this.club = club;
        this.user = user;
    }
}
