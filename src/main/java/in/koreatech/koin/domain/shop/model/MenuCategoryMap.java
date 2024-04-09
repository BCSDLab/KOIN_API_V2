package in.koreatech.koin.domain.shop.model;

import static lombok.AccessLevel.PROTECTED;

import org.hibernate.annotations.Where;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "shop_menu_category_map")
@NoArgsConstructor(access = PROTECTED)
public class MenuCategoryMap {

    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_menu_id")
    private Menu menu;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_menu_category_id")
    private MenuCategory menuCategory;

    @Builder
    private MenuCategoryMap(Long id, Menu menu, MenuCategory menuCategory) {
        this.id = id;
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
