package in.koreatech.koin.domain.order.cart.model;

import java.util.List;

import in.koreatech.koin.domain.order.cart.exception.CartErrorCode;
import in.koreatech.koin.domain.order.cart.exception.CartException;
import in.koreatech.koin.domain.order.shop.model.entity.menu.OrderableShopMenu;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OrderableShopMenus {

    private Integer orderableShopId;
    private List<OrderableShopMenu> menuList;

    public OrderableShopMenu resolveSelectedMenu(Integer selectedMenuId) {
        return menuList.stream()
            .filter(menu -> menu.getId().equals(selectedMenuId))
            .findFirst()
            .orElseThrow(() -> new CartException(CartErrorCode.INVALID_MENU_IN_SHOP));
    }
}
