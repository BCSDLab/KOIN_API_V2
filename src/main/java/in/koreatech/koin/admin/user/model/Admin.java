package in.koreatech.koin.admin.user.model;

import static lombok.AccessLevel.PROTECTED;

import in.koreatech.koin.admin.operators.enums.TeamType;
import in.koreatech.koin.admin.operators.enums.TrackType;
import in.koreatech.koin.domain.user.model.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
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
    @Column(name = "user_id", nullable = false)
    private Integer id;

    @NotNull
    @Size(max = 100)
    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    @NotNull
    @Size(max = 30)
    @Column(name = "login_id", nullable = false, unique = true, length = 30)
    private String loginId;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "team_type", nullable = false)
    private TeamType teamType;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "track_type", nullable = false)
    private TrackType trackType;

    @Column(name = "can_create_admin", columnDefinition = "TINYINT")
    private boolean canCreateAdmin = false;

    @Column(name = "super_admin", columnDefinition = "TINYINT")
    private boolean superAdmin = false;

    @MapsId
    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    @Builder
    private Admin(
        Integer id,
        String email,
        String loginId,
        TeamType teamType,
        TrackType trackType,
        boolean canCreateAdmin,
        boolean superAdmin,
        User user
        ) {
        this.id = id;
        this.email = email;
        this.loginId = loginId;
        this.teamType = teamType;
        this.trackType = trackType;
        this.canCreateAdmin = canCreateAdmin;
        this.superAdmin = superAdmin;
        this.user = user;
    }

    public void updateTeamTrack(TeamType teamName, TrackType trackName) {
        this.teamType = teamName;
        this.trackType = trackName;
    }

    /* 어드민 권한이 추가 되면, 해당 메소드에도 추가해야 합니다. */
    public void updatePermission(boolean canCreateAdmin, boolean superAdmin) {
        this.canCreateAdmin = canCreateAdmin;
        this.superAdmin = superAdmin;
    }
}
