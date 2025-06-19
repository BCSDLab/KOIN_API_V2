package in.koreatech.koin.domain.order.cart.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.order.cart.dto.CartAddItemCommand;
import in.koreatech.koin.domain.order.cart.exception.CartErrorCode;
import in.koreatech.koin.domain.order.cart.exception.CartException;
import in.koreatech.koin.domain.order.cart.model.Cart;
import in.koreatech.koin.domain.order.cart.model.CartMenuItem;
import in.koreatech.koin.domain.order.cart.model.MenuOptions;
import in.koreatech.koin.domain.order.cart.model.Menus;
import in.koreatech.koin.domain.order.cart.service.implement.CartDeleter;
import in.koreatech.koin.domain.order.cart.service.implement.CartGetter;
import in.koreatech.koin.domain.order.cart.service.implement.MenuGetter;
import in.koreatech.koin.domain.order.cart.service.implement.OptionGetter;
import in.koreatech.koin.domain.order.cart.service.implement.OrderableShopGetter;
import in.koreatech.koin.domain.order.cart.service.implement.UserGetter;
import in.koreatech.koin.domain.order.shop.model.entity.menu.OrderableShopMenu;
import in.koreatech.koin.domain.order.shop.model.entity.menu.OrderableShopMenuOption;
import in.koreatech.koin.domain.order.shop.model.entity.menu.OrderableShopMenuPrice;
import in.koreatech.koin.domain.order.shop.model.entity.shop.OrderableShop;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CartService {

    private final UserGetter userGetter;
    private final OrderableShopGetter orderableShopGetter;
    private final CartGetter cartGetter;
    private final CartDeleter cartDeleter;
    private final MenuGetter menuGetter;
    private final OptionGetter optionGetter;
    private final EntityManager em;

    @Transactional
    public void addMenu(CartAddItemCommand addItemCommand) {
        OrderableShop orderableShop = orderableShopGetter.getOrderableShop(addItemCommand.shopId());
        Cart cart = cartGetter.get(addItemCommand.userId()).orElse(
            Cart.from(userGetter.get(addItemCommand.userId()), orderableShop)
        );

        // 다른 상점 메뉴가 담겨 있는지 확인
        cart.validateSameShop(addItemCommand.shopId());

        // 선택한 메뉴가 해당 상점의 메뉴 인지 검증 하고, 엔티티 가져 오기
        Menus shopMenus = menuGetter.getAllByOrderableShopId(addItemCommand.shopId());
        OrderableShopMenu menu = shopMenus.resolveSelectedMenu(addItemCommand.shopMenuId());

        // 선택한 메뉴가 품절 상태 인지 확인
        menu.isNotSoldOut();
        // 선택한 메뉴 가격이 해당 메뉴의 가격 인지 검증
        menu.requiredMenuPriceById(addItemCommand.shopMenuPriceId());

        /*
          1. 선택한 메뉴 옵션이 선택한 메뉴 옵션 그룹에 포함 됐는지 확인
          2. 선택한 메뉴 옵션이 해당 메뉴의 옵션 인지 확인
          3. 선택한 메뉴 에서 필수 옵션이 선택 됐는지 검증
          4. 선택한 메뉴 옵션의 옵션 그룹 조건 (minSelect, maxSelect) 검증
          5. 검증 모두 통과 하면 엔티티 가져 오기
        */
        MenuOptions shopMenuOptions = optionGetter.getAllByMenuId(addItemCommand.shopMenuId());
        List<OrderableShopMenuOption> selectedOptions = shopMenuOptions.resolveSelectedOptions(addItemCommand.options());

        // 메뉴 추가
        cart.addItem(menu, menu.getMenuPriceById(addItemCommand.shopMenuPriceId()), selectedOptions);
        em.persist(cart);
    }

    @Transactional
    public void updateMenuQuantity(Integer userId, Integer cartMenuItemID, Integer quantity) {
        Cart cart = cartGetter.get(userId)
            .orElseThrow(() -> new CartException(CartErrorCode.CART_NOT_FOUND));
        cart.updateItemQuantity(cartMenuItemID, quantity);
    }

    @Transactional
    public void deleteMenu(Integer userId, Integer cartMenuItemID) {
        Cart cart = cartGetter.get(userId)
            .orElseThrow(() -> new CartException(CartErrorCode.CART_NOT_FOUND));
        cart.removeItem(cartMenuItemID);
    }

    @Transactional
    public void resetCart(Integer userId) {
        cartGetter.get(userId)
            .orElseThrow(() -> new CartException(CartErrorCode.CART_NOT_FOUND));
        cartDeleter.deleteByUserId(userId);
    }
}
