package in.koreatech.koin.admin.user.model;

import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

import in.koreatech.koin.admin.user.enums.Role;
import in.koreatech.koin.admin.user.enums.TeamType;
import in.koreatech.koin.admin.user.enums.TrackType;
import in.koreatech.koin.domain.user.model.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "admins")
@NoArgsConstructor(access = PROTECTED)
public class Admin {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Integer id;

    @Size(max = 10)
    @Column(name = "name")
    private String name;

    @Size(max = 100)
    @Column(name = "email")
    private String email;

    @Size(max = 20)
    @Column(name = "phone_number")
    private String phoneNumber;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "team_type", nullable = false)
    private TeamType teamType;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "track_type", nullable = false)
    private TrackType trackType;

    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private Role role;

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    @Builder
    private Admin(
        Integer id,
        String name,
        String email,
        String phoneNumber,
        TeamType teamType,
        TrackType trackType,
        Role role,
        User user
    ) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.teamType = teamType;
        this.trackType = trackType;
        this.role = role;
        this.user = user;
    }

    public void updateTeamTrack(TeamType teamName, TrackType trackName) {
        this.teamType = teamName;
        this.trackType = trackName;
    }
}
