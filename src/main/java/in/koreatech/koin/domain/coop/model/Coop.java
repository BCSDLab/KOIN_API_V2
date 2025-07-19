package in.koreatech.koin.domain.coop.model;

import static lombok.AccessLevel.PROTECTED;

import in.koreatech.koin.domain.user.model.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "coop")
@NoArgsConstructor(access = PROTECTED)
public class Coop {

    @Id
    @Column(name = "user_id")
    private Integer id;

    @NotNull
    @Size(max = 30)
    @Column(name = "coop_id", nullable = false, unique = true, length = 30)
    private String loginId;

    @OneToOne
    @MapsId
    private User user;

    @Builder
    private Coop(
        String loginId,
        User user
    ) {
        this.loginId = loginId;
        this.user = user;
    }
}
