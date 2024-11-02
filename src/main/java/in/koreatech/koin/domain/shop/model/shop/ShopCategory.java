package in.koreatech.koin.domain.shop.model.shop;

import static jakarta.persistence.CascadeType.PERSIST;
import static jakarta.persistence.CascadeType.REMOVE;
import static jakarta.persistence.GenerationType.IDENTITY;

import java.util.ArrayList;
import java.util.List;

import in.koreatech.koin.global.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
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

    @NotNull
    @Column(name = "sort_order", nullable = false)
    @PositiveOrZero
    private Integer sortOrder;

    @OneToMany(mappedBy = "shopCategory", orphanRemoval = true, cascade = {PERSIST, REMOVE})
    private List<ShopCategoryMap> shopCategoryMaps = new ArrayList<>();

    @Builder
    private ShopCategory(String name, String imageUrl, Integer sortOrder) {
        this.name = name;
        this.imageUrl = imageUrl;
        this.sortOrder = sortOrder;
    }

    public void modifyShopCategory(String name, String imageUrl, Integer sortOrder) {
        this.name = name;
        this.imageUrl = imageUrl;
        this.sortOrder = sortOrder;
    }
}
