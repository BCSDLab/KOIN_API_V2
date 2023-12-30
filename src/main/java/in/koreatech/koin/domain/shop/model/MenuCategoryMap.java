package in.koreatech.koin.domain.shop.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "shop_menu_category_map")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MenuCategoryMap {

    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_menu_id")
    private Menu menu;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "shop_menu_category_id")
    private MenuCategory menuCategory;

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
