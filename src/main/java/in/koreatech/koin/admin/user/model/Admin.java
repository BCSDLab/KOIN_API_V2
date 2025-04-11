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

    @Column(name = "name")
    private String name;

    @Column(name = "email")
    private String email;

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

    @Column(name = "can_create_admin", columnDefinition = "TINYINT")
    private boolean canCreateAdmin = false;

    @Column(name = "super_admin", columnDefinition = "TINYINT")
    private boolean superAdmin = false;

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
        boolean canCreateAdmin,
        boolean superAdmin,
        Role role,
        User user
    ) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.teamType = teamType;
        this.trackType = trackType;
        this.canCreateAdmin = canCreateAdmin;
        this.superAdmin = superAdmin;
        this.role = role;
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
