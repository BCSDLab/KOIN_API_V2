package in.koreatech.koin.domain.coopshop.model;

import static jakarta.persistence.CascadeType.*;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

import java.util.ArrayList;
import java.util.List;

import in.koreatech.koin.common.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "coop_shop")
@NoArgsConstructor(access = PROTECTED)
public class CoopShop extends BaseEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "semester_id", referencedColumnName = "id", nullable = false)
    private CoopSemester coopSemester;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coop_name_id", referencedColumnName = "id", nullable = false)
    private CoopName coopName;

    @NotNull
    @Column(name = "phone", nullable = false)
    private String phone;

    @NotNull
    @Column(name = "location", nullable = false)
    private String location;

    @Column(name = "remarks")
    private String remarks;

    @OneToMany(mappedBy = "coopShop", orphanRemoval = true, cascade = {PERSIST, REFRESH, MERGE, REMOVE},
        fetch = FetchType.EAGER)
    private final List<CoopOpen> coopOpens = new ArrayList<>();

    @Builder
    private CoopShop(
        CoopSemester coopSemester,
        CoopName coopName,
        String phone,
        String location,
        String remarks
    ) {
        this.coopSemester = coopSemester;
        this.coopName = coopName;
        this.phone = phone;
        this.location = location;
        this.remarks = remarks;
    }
}
