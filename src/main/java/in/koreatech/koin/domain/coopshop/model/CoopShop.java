package in.koreatech.koin.domain.coopshop.model;

import static jakarta.persistence.CascadeType.*;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

import java.util.ArrayList;
import java.util.List;

import in.koreatech.koin.global.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
    private CoopShopSemester coopShopSemester;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "name", nullable = false)
    private CoopShopType name;

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
        CoopShopSemester coopShopSemester,
        CoopShopType name,
        String phone,
        String location,
        String remarks
    ) {
        this.coopShopSemester = coopShopSemester;
        this.name = name;
        this.phone = phone;
        this.location = location;
        this.remarks = remarks;
    }
}
