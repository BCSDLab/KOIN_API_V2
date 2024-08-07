package in.koreatech.koin.domain.coop.model;

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

@Getter
@Entity
@Table(name = "coop")
@NoArgsConstructor(access = PROTECTED)
public class Coop {

    @Id
    @Column(name = "user_id")
    private Integer id;

    @Size(max = 255)
    @Column(name = "coop_id")
    private String coopId;

    @OneToOne
    @MapsId
    private User user;

    @Builder
    private Coop(
        String coopId,
        User user
    ) {
        this.coopId = coopId;
        this.user = user;
    }
}
