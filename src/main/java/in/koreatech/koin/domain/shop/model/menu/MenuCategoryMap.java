package in.koreatech.koin.domain.shop.model.menu;

import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

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
@Table(name = "shop_menu_category_map", uniqueConstraints = {
    @UniqueConstraint(
        name = "SHOP_MENU_ID_AND_SHOP_MENU_CATEGORY_ID",
        columnNames = {"shop_menu_id", "shop_menu_category_id"}
    )}
)
@NoArgsConstructor(access = PROTECTED)
public class MenuCategoryMap {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_menu_id")
    private Menu menu;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_menu_category_id")
    private MenuCategory menuCategory;

    @Builder
    private MenuCategoryMap(Menu menu, MenuCategory menuCategory) {
        this.menu = menu;
        this.menuCategory = menuCategory;
    }

    public static MenuCategoryMap create() {
        return new MenuCategoryMap();
    }

    public void map(Menu menu, MenuCategory menuCategory) {
        setMenu(menu);
        setMenuCategory(menuCategory);
    }

    private void setMenu(Menu menu) {
        if (menu.equals(this.menu)) {
            return;
        }

        if (this.menu != null) {
            this.menu.getMenuCategoryMaps().remove(this);
        }

        this.menu = menu;
        menu.getMenuCategoryMaps().add(this);
    }

    private void setMenuCategory(MenuCategory menuCategory) {
        if (menuCategory.equals(this.menuCategory)) {
            return;
        }

        if (this.menuCategory != null) {
            this.menuCategory.getMenuCategoryMaps().remove(this);
        }

        this.menuCategory = menuCategory;
        menuCategory.getMenuCategoryMaps().add(this);
    }
}
