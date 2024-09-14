package in.koreatech.koin.domain.benefit.model;

import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

import in.koreatech.koin.domain.shop.model.shop.Shop;
import in.koreatech.koin.global.domain.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "shop_benefit_category_map")
@NoArgsConstructor(access = PROTECTED)
public class BenefitCategoryMap extends BaseEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id", referencedColumnName = "id", nullable = false)
    private Shop shop;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "benefit_id", referencedColumnName = "id", nullable = false)
    private BenefitCategory benefitCategory;

    @Builder
    public BenefitCategoryMap(Shop shop, BenefitCategory benefitCategory) {
        this.shop = shop;
        this.benefitCategory = benefitCategory;
    }
}
