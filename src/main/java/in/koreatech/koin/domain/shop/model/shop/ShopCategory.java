package in.koreatech.koin.domain.shop.model.shop;

import static jakarta.persistence.CascadeType.PERSIST;
import static jakarta.persistence.CascadeType.REMOVE;
import static jakarta.persistence.GenerationType.IDENTITY;

import java.util.ArrayList;
import java.util.List;

import in.koreatech.koin._common.model.BaseEntity;
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
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "shop_categories")
public class ShopCategory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Integer id;

    @Size(max = 255)
    @Column(name = "name", nullable = false)
    private String name;

    @Size(max = 255)
    @Column(name = "image_url")
    private String imageUrl;

    @Size(max = 255)
    @Column(name = "event_banner_image_url")
    private String eventBannerImageUrl;

    @NotNull
    @PositiveOrZero
    @Column(name = "order_index", nullable = false)
    private Integer orderIndex = 0;

    @OneToMany(mappedBy = "shopCategory", orphanRemoval = true, cascade = {PERSIST, REMOVE})
    private List<ShopCategoryMap> shopCategoryMaps = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_category_id", referencedColumnName = "id")
    private ShopParentCategory parentCategory;

    @Builder
    private ShopCategory(
        String name,
        String imageUrl,
        ShopParentCategory parentCategory,
        String eventBannerImageUrl,
        Integer orderIndex
    ) {
        this.name = name;
        this.imageUrl = imageUrl;
        this.parentCategory = parentCategory;
        this.eventBannerImageUrl = eventBannerImageUrl;
        this.orderIndex = orderIndex == null ? 0 : orderIndex;
    }

    public void modifyShopCategory(String name, String imageUrl, ShopParentCategory parentCategory, String eventBannerImageUrl) {
        this.name = name;
        this.imageUrl = imageUrl;
        this.parentCategory = parentCategory;
        this.eventBannerImageUrl = eventBannerImageUrl;
    }

    public void modifyOrderIndex(Integer orderIndex) {
        this.orderIndex = orderIndex;
    }
}
