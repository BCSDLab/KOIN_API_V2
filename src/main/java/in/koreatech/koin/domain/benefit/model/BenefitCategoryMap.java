package in.koreatech.koin.domain.benefit.model;

import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

import in.koreatech.koin.domain.shop.model.shop.Shop;
import in.koreatech.koin._common.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
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

    @Size(min = 2, max = 20)
    @Column(name = "detail")
    private String detail;

    @Builder
    public BenefitCategoryMap(Shop shop, BenefitCategory benefitCategory, String detail) {
        this.shop = shop;
        this.benefitCategory = benefitCategory;
        this.detail = detail;
    }

    public void modifyDetail(String detail) {
        this.detail = detail;
    }
}
