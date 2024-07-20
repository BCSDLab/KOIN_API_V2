package in.koreatech.koin.domain.dining.model;

import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "dining_likes")
@NoArgsConstructor(access = PROTECTED)
public class DiningLikes {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "dining_id", nullable = false)
    private Integer diningId;

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Builder
    private DiningLikes(
        Integer diningId,
        Integer userId
    ) {
        this.diningId = diningId;
        this.userId = userId;
    }
}
