package in.koreatech.koin.domain.shop.model;

import static jakarta.persistence.CascadeType.ALL;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import in.koreatech.koin.global.domain.BaseEntity;
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
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "shop_menu_categories")
@NoArgsConstructor(access = PROTECTED)
public final class MenuCategory extends BaseEntity {

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

    @Builder
    private MenuCategory(Shop shop, String name) {
        this.shop = shop;
        this.name = name;
    }

    public void modifyName(String name) {
        this.name = name;
    }

    public static void sortMenuCategories(List<MenuCategory> menuCategories) {
        Map<String, Integer> priorityMap = new HashMap<>();
        priorityMap.put("추천 메뉴", 1);
        priorityMap.put("메인 메뉴", 2);
        priorityMap.put("세트 메뉴", 3);
        priorityMap.put("사이드 메뉴", 4);
        Collections.sort(menuCategories, (cat1, cat2) -> {
            Integer priority1 = priorityMap.getOrDefault(cat1.getName(), 5);
            Integer priority2 = priorityMap.getOrDefault(cat2.getName(), 5);
            return priority1.compareTo(priority2);
        });
    }
}
