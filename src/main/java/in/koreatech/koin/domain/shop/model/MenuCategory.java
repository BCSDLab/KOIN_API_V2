package in.koreatech.koin.domain.shop.model;

import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import in.koreatech.koin.domain.shop.dto.ModifyCategoryRequest;
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
@Where(clause = "is_deleted=0")
@SQLDelete(sql = "UPDATE shop_menu_categories SET is_deleted = true WHERE id = ?")
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

    @NotNull
    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;

    @OneToMany(mappedBy = "menuCategory")
    private List<MenuCategoryMap> menuCategoryMaps = new ArrayList<>();

    @Builder
    private MenuCategory(Shop shop, String name) {
        this.shop = shop;
        this.name = name;
    }

    public void modifyCategory(ModifyCategoryRequest modifyCategoryRequest) {
        this.name = modifyCategoryRequest.name();
    }
}
