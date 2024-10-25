package in.koreatech.koin.admin.user.model;

import static lombok.AccessLevel.PROTECTED;

import in.koreatech.koin.domain.user.model.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
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
    @Column(name = "user_id")
    private Integer id;

    @Size(max = 20)
    @Column(name = "team_name")
    private String teamName;

    @Size(max = 20)
    @Column(name = "track_name")
    private String trackName;

    @Column(name = "create_admin")
    private boolean createAdmin = false;

    @Column(name = "approve_admin")
    private boolean approveAdmin = false;

    @OneToOne
    @MapsId
    private User user;

    @Builder
    public Admin(Integer id, String teamName, String trackName, boolean createAdmin, boolean approveAdmin, User user) {
        this.id = id;
        this.teamName = teamName;
        this.trackName = trackName;
        this.createAdmin = createAdmin;
        this.approveAdmin = approveAdmin;
        this.user = user;
    }
}
