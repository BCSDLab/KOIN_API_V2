package in.koreatech.koin.domain.shop.model.menu;

import static jakarta.persistence.CascadeType.ALL;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

import in.koreatech.koin.admin.shop.dto.menu.AdminModifyMenuRequest;
import in.koreatech.koin.domain.shop.dto.menu.ModifyMenuRequest;
import in.koreatech.koin.domain.shop.dto.menu.ModifyMenuRequest.InnerOptionPrice;
import in.koreatech.koin.domain.shop.model.shop.Shop;
import in.koreatech.koin.global.domain.BaseEntity;
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
import java.util.ArrayList;
import java.util.List;
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
        for (String imageUrl : imageUrls) {
            MenuImage newMenuImage = MenuImage.builder()
                .imageUrl(imageUrl)
                .menu(this)
                .build();
            this.menuImages.add(newMenuImage);
        }
    }

    public void modifyMenuCategories(List<MenuCategory> menuCategories, EntityManager entityManager) {
        this.menuCategoryMaps.clear();
        entityManager.flush();
        for (MenuCategory menuCategory : menuCategories) {
            MenuCategoryMap menuCategoryMap = MenuCategoryMap.builder()
                .menu(this)
                .menuCategory(menuCategory)
                .build();
            this.menuCategoryMaps.add(menuCategoryMap);
        }
    }

    public void modifyMenuSingleOptions(ModifyMenuRequest modifyMenuRequest, EntityManager entityManager) {
        this.menuOptions.clear();
        entityManager.flush();
        MenuOption menuOption = MenuOption.builder()
            .price(modifyMenuRequest.singlePrice())
            .menu(this)
            .build();
        this.menuOptions.add(menuOption);
    }

    public void adminModifyMenuSingleOptions(AdminModifyMenuRequest adminModifyMenuRequest,
        EntityManager entityManager) {
        this.menuOptions.clear();
        entityManager.flush();
        MenuOption menuOption = MenuOption.builder()
            .price(adminModifyMenuRequest.singlePrice())
            .menu(this)
            .build();
        this.menuOptions.add(menuOption);
    }

    public void modifyMenuMultipleOptions(List<InnerOptionPrice> innerOptionPrice, EntityManager entityManager) {
        this.menuOptions.clear();
        entityManager.flush();
        for (var option : innerOptionPrice) {
            MenuOption menuOption = MenuOption.builder()
                .option(option.option())
                .price(option.price())
                .menu(this)
                .build();
            this.menuOptions.add(menuOption);
        }
    }

    public void adminModifyMenuMultipleOptions(List<AdminModifyMenuRequest.InnerOptionPrice> innerOptionPrice,
        EntityManager entityManager) {
        this.menuOptions.clear();
        entityManager.flush();
        for (var option : innerOptionPrice) {
            MenuOption menuOption = MenuOption.builder()
                .option(option.option())
                .price(option.price())
                .menu(this)
                .build();
            this.menuOptions.add(menuOption);
        }
    }
}
