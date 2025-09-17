package in.koreatech.koin.domain.club.category.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

@Entity
@Getter
@Table(name = "club_category")
@NoArgsConstructor(access = PROTECTED)
public class ClubCategory {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Integer id;

    @NotNull
    @Column(name = "name", nullable = false)
    private String name;

    @Builder
    private ClubCategory(
        Integer id,
        String name
    ) {
        this.id = id;
        this.name = name;
    }
}
