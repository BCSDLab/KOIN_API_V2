package in.koreatech.koin.domain.order.shop.model.entity.menu;

import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Where;

import in.koreatech.koin._common.model.BaseEntity;
import in.koreatech.koin.domain.order.cart.dto.CartAddItemCommand;
import in.koreatech.koin.domain.order.cart.exception.CartErrorCode;
import in.koreatech.koin.domain.order.cart.exception.CartException;
import in.koreatech.koin.domain.order.shop.model.entity.shop.OrderableShop;
import jakarta.persistence.CascadeType;
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
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = PROTECTED)
@Table(name = "orderable_shop_menu")
@Where(clause = "is_deleted=0")
public class OrderableShopMenu extends BaseEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "orderable_shop_id", referencedColumnName = "id", nullable = false)
    private OrderableShop orderableShop;

    @NotNull
    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", columnDefinition = "text")
    private String description;

    @NotNull
    @Column(name = "is_sold_out", nullable = false)
    private Boolean isSoldOut = false;

    @NotNull
    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    @OneToMany(mappedBy = "menu", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderableShopMenuOptionGroupMap> menuOptionGroupMap = new ArrayList<>();

    @OneToMany(mappedBy = "menu", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderableShopMenuPrice> menuPrices = new ArrayList<>();

    @OneToMany(mappedBy = "menu", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderableShopMenuImage> menuImages = new ArrayList<>();

    public void validateSoldOut() {
        if(this.isSoldOut) {
           throw new CartException(CartErrorCode.MENU_SOLD_OUT);
        }
    }

    public void requiredMenuPriceById(Integer menuPriceId) {
        boolean exists = menuPrices.stream()
            .anyMatch(menuPrice -> menuPrice.getId().equals(menuPriceId));
        if(!exists) {
            throw new CartException(CartErrorCode.MENU_PRICE_NOT_FOUND);
        }
    }

    public OrderableShopMenuPrice getMenuPriceById(Integer menuPriceId) {
        return menuPrices.stream()
            .filter(menuPrice -> menuPrice.getId().equals(menuPriceId))
            .findFirst()
            .orElse(null);
    }

    public String getThumbnailImage() {
        return this.menuImages.stream()
            .filter(OrderableShopMenuImage::getIsThumbnail)
            .map(OrderableShopMenuImage::getImageUrl)
            .findFirst()
            .orElse(null);
    }
}
