package in.koreatech.koin.domain.shop.model.shop;

import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

import in.koreatech.koin._common.model.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = PROTECTED)
@Table(name = "shop_category_map",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "SHOP_ID_AND_SHOP_CATEGORY_ID",
            columnNames = {"shop_id", "shop_category_id"}
        )
    }
)
public class ShopCategoryMap extends BaseEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id", referencedColumnName = "id", nullable = false)
    private Shop shop;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_category_id", referencedColumnName = "id", nullable = false)
    private ShopCategory shopCategory;

    @Builder
    private ShopCategoryMap(Shop shop, ShopCategory shopCategory) {
        this.shop = shop;
        this.shopCategory = shopCategory;
    }
}
