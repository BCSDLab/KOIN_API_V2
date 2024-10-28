package in.koreatech.koin.admin.user.model;

import static lombok.AccessLevel.PROTECTED;

import in.koreatech.koin.domain.user.model.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

    @MapsId
    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    @NotNull
    @Size(max = 10)
    @Column(name = "team_name", length = 10, nullable = false)
    private String teamNName;

    @NotNull
    @Size(max = 20)
    @Column(name = "track_name", length = 20, nullable = false)
    private String trackName;

    @Column(name = "create_admin", columnDefinition = "TINYINT")
    private boolean createAdmin = false;

    @Column(name = "super_admin", columnDefinition = "TINYINT")
    private boolean superAdmin = false;

    @Builder
    public Admin(User user, String teamNName, String trackName, boolean createAdmin, boolean superAdmin) {
        this.user = user;
        this.teamNName = teamNName;
        this.trackName = trackName;
        this.createAdmin = createAdmin;
        this.superAdmin = superAdmin;
    }
}
