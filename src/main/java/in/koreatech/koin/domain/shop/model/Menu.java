package in.koreatech.koin.domain.shop.model;

import static jakarta.persistence.CascadeType.MERGE;
import static jakarta.persistence.CascadeType.PERSIST;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import in.koreatech.koin.domain.shop.dto.ModifyMenuRequest;
import in.koreatech.koin.domain.shop.dto.ModifyMenuRequest.InnerOptionPrice;
import in.koreatech.koin.global.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
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
@Where(clause = "is_deleted=0")
@SQLDelete(sql = "UPDATE shop_menus SET is_deleted = true WHERE id = ?")
@NoArgsConstructor(access = PROTECTED)
public class Menu extends BaseEntity {

    private static final int SINGLE_OPTION_COUNT = 1;

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Integer id;

    @NotNull
    @Column(name = "shop_id", nullable = false)
    private Integer shopId;

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

    @NotNull
    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;

    @OneToMany(mappedBy = "menu", cascade = {MERGE, PERSIST})
    private List<MenuCategoryMap> menuCategoryMaps = new ArrayList<>();

    @OneToMany(mappedBy = "menu", cascade = {MERGE, PERSIST})
    private List<MenuOption> menuOptions = new ArrayList<>();

    @OneToMany(mappedBy = "menu", cascade = {MERGE, PERSIST})
    private List<MenuImage> menuImages = new ArrayList<>();

    @Builder
    private Menu(Integer shopId, String name, String description) {
        this.shopId = shopId;
        this.name = name;
        this.description = description;
    }

    public boolean hasMultipleOption() {
        return menuOptions.size() > SINGLE_OPTION_COUNT;
    }

    @Override
    public String toString() {
        return "Menu{" +
            "id=" + id +
            ", shopId=" + shopId +
            ", name='" + name + '\'' +
            '}';
    }

    public void modifyMenu(
        String name,
        String description
    ) {
        this.name = name;
        this.description = description;
    }

    public void modifyMenuImages(List<String> imageUrls, EntityManager entityManager) {
        this.menuImages.forEach(entityManager::remove);
        this.menuImages.clear();
        for (String imageUrl : imageUrls) {
            MenuImage newMenuImage = MenuImage.builder()
                .imageUrl(imageUrl)
                .menu(this)
                .build();
            this.menuImages.add(newMenuImage);
        }
    }

    public void modifyMenuCategories(List<MenuCategory> menuCategories, EntityManager entityManager) {
        this.menuCategoryMaps.forEach(entityManager::remove);
        this.menuCategoryMaps.clear();
        for (MenuCategory menuCategory : menuCategories) {
            MenuCategoryMap menuCategoryMap = MenuCategoryMap.builder()
                .menu(this)
                .menuCategory(menuCategory)
                .build();
            this.menuCategoryMaps.add(menuCategoryMap);
        }
    }

    public void modifyMenuSingleOptions(ModifyMenuRequest modifyMenuRequest, EntityManager entityManager) {
        this.menuOptions.forEach(entityManager::remove);
        this.menuOptions.clear();
        MenuOption menuOption = MenuOption.builder()
            .price(modifyMenuRequest.singlePrice())
            .menu(this)
            .build();
        this.menuOptions.add(menuOption);
    }

    public void modifyMenuMultipleOptions(List<InnerOptionPrice> innerOptionPrice, EntityManager entityManager) {
        this.menuOptions.forEach(entityManager::remove);
        this.menuOptions.clear();
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
