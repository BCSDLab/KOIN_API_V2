package in.koreatech.koin.domain.coopshop.model;

import static jakarta.persistence.CascadeType.*;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import in.koreatech.koin.common.model.BaseEntity;
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
@Table(name = "coop_semester")
@NoArgsConstructor(access = PROTECTED)
public class CoopSemester extends BaseEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Integer id;

    @NotNull
    @Column(name = "semester", nullable = false)
    private String semester;

    @NotNull
    @Column(name = "from_date", nullable = false)
    private LocalDate fromDate;

    @NotNull
    @Column(name = "to_date", nullable = false)
    private LocalDate toDate;

    @NotNull
    @Column(name = "is_applied", columnDefinition = "TINYINT", nullable = false)
    private boolean isApplied = false;

    @OneToMany(mappedBy = "coopSemester", orphanRemoval = true, cascade = {PERSIST, REFRESH, MERGE, REMOVE},
        fetch = FetchType.EAGER)
    private List<CoopShop> coopShops = new ArrayList<>();

    @Builder
    private CoopSemester(
        String semester,
        LocalDate fromDate,
        LocalDate toDate
    ) {
        this.semester = semester;
        this.fromDate = fromDate;
        this.toDate = toDate;
    }

    public void updateApply(boolean isApplied) {
        this.isApplied = isApplied;
    }
}
