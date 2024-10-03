package in.koreatech.koin.domain.coopshop.model;

import static jakarta.persistence.CascadeType.*;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

import java.util.ArrayList;
import java.util.List;

import in.koreatech.koin.global.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "coop_shop_semester")
@NoArgsConstructor(access = PROTECTED)
public class CoopShopSemester extends BaseEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Integer id;

    @NotNull
    @Column(name = "semester", nullable = false)
    private String semester;

    @NotNull
    @Column(name = "term", nullable = false)
    private String term;

    @NotNull
    @Column(name = "is_applied", columnDefinition = "TINYINT", nullable = false)
    private final boolean isApplied = true;

    @OneToMany(mappedBy = "coopShopSemester", orphanRemoval = true, cascade = {PERSIST, REFRESH, MERGE, REMOVE},
        fetch = FetchType.EAGER)
    private final List<CoopShop> coopShops = new ArrayList<>();

    @Builder
    private CoopShopSemester(
        String semester,
        String term
    ) {
        this.semester = semester;
        this.term = term;
    }
}
