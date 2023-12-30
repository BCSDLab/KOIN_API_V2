package in.koreatech.koin.domain.shop.model;

import java.util.ArrayList;
import java.util.List;

import in.koreatech.koin.global.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "shop_menu_categories")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public final class MenuCategory extends BaseEntity {

    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "shop_id", nullable = false)
    private Long shopId;

    @Size(max = 255)
    @NotNull
    @Column(name = "name", nullable = false)
    private String name;

    @NotNull
    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    @OneToMany(mappedBy = "menuCategory")
    private List<MenuCategoryMap> menuCategoryMaps = new ArrayList<>();

    @Builder
    private MenuCategory(Long shopId, String name) {
        this.shopId = shopId;
        this.name = name;
    }
}
