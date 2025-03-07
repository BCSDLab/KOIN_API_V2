package in.koreatech.koin.domain.shop.model.menu;

import static jakarta.persistence.CascadeType.ALL;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

import java.util.ArrayList;
import java.util.List;

import in.koreatech.koin.domain.shop.model.shop.Shop;
import in.koreatech.koin._common.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
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
@Table(name = "shop_menus")
@NoArgsConstructor(access = PROTECTED)
public class Menu extends BaseEntity {

    private static final int SINGLE_OPTION_COUNT = 1;

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id", referencedColumnName = "id", nullable = false)
    private Shop shop;

    @Size(max = 255)
    @NotNull
    @Column(name = "name", nullable = false)
    private String name;

    @Size(max = 255)
    @Column(name = "description")
    private String description;

    @NotNull
    @Column(name = "is_hidden", nullable = false)
    private boolean isHidden = false;

    @OneToMany(mappedBy = "menu", orphanRemoval = true, cascade = ALL)
    private List<MenuCategoryMap> menuCategoryMaps = new ArrayList<>();

    @OneToMany(mappedBy = "menu", orphanRemoval = true, cascade = ALL)
    private List<MenuOption> menuOptions = new ArrayList<>();

    @OneToMany(mappedBy = "menu", orphanRemoval = true, cascade = ALL)
    private List<MenuImage> menuImages = new ArrayList<>();

    @Builder
    private Menu(
        Shop shop,
        String name,
        String description
    ) {
        this.shop = shop;
        this.name = name;
        this.description = description;
    }

    public boolean hasMultipleOption() {
        return menuOptions.size() > SINGLE_OPTION_COUNT;
    }

    public void modifyMenu(
        String name,
        String description
    ) {
        this.name = name;
        this.description = description;
    }

    public void modifyMenuImages(List<String> imageUrls, EntityManager entityManager) {
        this.menuImages.clear();
        entityManager.flush();
        addMenuImages(imageUrls);
    }

    public void modifyMenuCategories(List<MenuCategory> menuCategories, EntityManager entityManager) {
        this.menuCategoryMaps.clear();
        entityManager.flush();
        addMenuCategories(menuCategories);
    }

    public void modifyOptions(List<MenuOption> menuOptions, EntityManager entityManager) {
        this.menuOptions.clear();
        entityManager.flush();
        addMenuOptions(menuOptions);
    }

    public void addMenuCategories(List<MenuCategory> menuCategories) {
        for (MenuCategory menuCategory : menuCategories) {
            MenuCategoryMap menuCategoryMap = MenuCategoryMap.builder()
                .menu(this)
                .menuCategory(menuCategory)
                .build();
            this.menuCategoryMaps.add(menuCategoryMap);
        }
    }

    public void addMenuImages(List<String> imageUrls) {
        for (String imageUrl : imageUrls) {
            MenuImage menuImage = MenuImage.builder()
                .imageUrl(imageUrl)
                .menu(this)
                .build();
            this.menuImages.add(menuImage);
        }
    }

    public void addMenuOptions(List<MenuOption> menuOptions) {
        this.menuOptions.addAll(menuOptions);
    }
}
