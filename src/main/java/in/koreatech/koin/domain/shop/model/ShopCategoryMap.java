package in.koreatech.koin.domain.shop.model;

import org.hibernate.annotations.Where;

import in.koreatech.koin.domain.owner.domain.Owner;
import in.koreatech.koin.global.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "shop_category_map")
@Where(clause = "is_deleted=0")
public class ShopCategoryMap extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id", referencedColumnName = "id", nullable = false)
    private Shop shop;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_category_id", referencedColumnName = "id", nullable = false)
    private ShopCategory shopCategory;

    @Builder
    public ShopCategoryMap(Long id, Shop shop, ShopCategory shopCategory) {
        this.id = id;
        this.shop = shop;
        this.shopCategory = shopCategory;
    }
}
