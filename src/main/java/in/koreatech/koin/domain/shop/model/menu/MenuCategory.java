package in.koreatech.koin.domain.shop.model.menu;

import static jakarta.persistence.CascadeType.ALL;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import in.koreatech.koin.domain.shop.model.shop.Shop;
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
import jakarta.persistence.Transient;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "shop_menu_categories")
@NoArgsConstructor(access = PROTECTED)
public class MenuCategory extends BaseEntity implements Comparable<MenuCategory> {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id", nullable = false)
    private Shop shop;

    @Size(max = 255)
    @NotNull
    @Column(name = "name", nullable = false)
    private String name;

    @OneToMany(mappedBy = "menuCategory", orphanRemoval = true, cascade = ALL)
    private List<MenuCategoryMap> menuCategoryMaps = new ArrayList<>();

    @Transient
    private Map<String, Integer> priorityMap = Map.of(
        "추천 메뉴", 1,
        "메인 메뉴", 2,
        "세트 메뉴", 3,
        "사이드 메뉴", 4
    );

    @Builder
    private MenuCategory(Shop shop, String name) {
        this.shop = shop;
        this.name = name;
    }

    public void modifyName(String name) {
        this.name = name;
    }

    @Override
    public int compareTo(MenuCategory other) {
        return priorityMap.getOrDefault(name, 5) - priorityMap.getOrDefault(other.name, 5);
    }
}
