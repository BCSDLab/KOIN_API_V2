package in.koreatech.koin.domain.order.cart.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.order.cart.dto.CartAddItemCommand;
import in.koreatech.koin.domain.order.cart.dto.CartUpdateItemRequest;
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
    public void addItem(CartAddItemCommand addItemCommand) {
        // 상점이 영업 시간 인지 확인
        OrderableShop orderableShop = orderableShopGetter.getOrderableShop(addItemCommand.shopId());
        orderableShop.requireShopOpen();

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
    public void updateItemQuantity(Integer userId, Integer cartMenuItemID, Integer quantity) {
        Cart cart = cartGetter.get(userId)
            .orElseThrow(() -> new CartException(CartErrorCode.CART_NOT_FOUND));
        cart.updateItemQuantity(cartMenuItemID, quantity);
    }

    @Transactional
    public void deleteItem(Integer userId, Integer cartMenuItemID) {
        Cart cart = cartGetter.get(userId)
            .orElseThrow(() -> new CartException(CartErrorCode.CART_NOT_FOUND));

        cart.removeItem(cartMenuItemID);

        // 장바구니가 비어있으면 장바구니 자체를 삭제
        if (cart.getCartMenuItems().isEmpty()) {
            cartDeleter.deleteByUserId(userId);
        }
    }

    @Transactional
    public void updateItem(CartUpdateItemRequest request, Integer cartMenuItemId, Integer userId) {
        Cart cart = cartGetter.get(userId)
            .orElseThrow(() -> new CartException(CartErrorCode.CART_NOT_FOUND));
        CartMenuItem itemToUpdate = cart.getCartMenuItem(cartMenuItemId);

        OrderableShopMenu menu = itemToUpdate.getOrderableShopMenu();
        Integer originalQuantity = itemToUpdate.getQuantity();

        // 새로운 가격 및 옵션 구성의 유효성 검증
        menu.requiredMenuPriceById(request.orderableShopMenuPriceId());
        OrderableShopMenuPrice newPrice = menu.getMenuPriceById(request.orderableShopMenuPriceId());

        MenuOptions shopMenuOptions = optionGetter.getAllByMenuId(menu.getId());
        List<OrderableShopMenuOption> newSelectedOptions = shopMenuOptions.resolveSelectedOptions(request.toOptions());

        // 변경 후의 구성이 장바구니에 이미 존재하는지 확인
        Optional<CartMenuItem> existingSameItem = cart.findSameItem(menu, newPrice, newSelectedOptions, itemToUpdate.getId());

        if (existingSameItem.isPresent()) {
            // 동일 구성 아이템이 존재하면 수량을 합치고 기존 아이템은 삭제
            existingSameItem.get().increaseQuantity(originalQuantity);
            cart.removeItem(itemToUpdate.getId());
        } else {
            // 동일 구성 아이템이 없으면 현재 아이템의 구성을 직접 변경
            itemToUpdate.updatePriceAndOptions(newPrice, newSelectedOptions);
        }
    }

    @Transactional
    public void resetCart(Integer userId) {
        cartGetter.get(userId)
            .orElseThrow(() -> new CartException(CartErrorCode.CART_NOT_FOUND));
        cartDeleter.deleteByUserId(userId);
    }
}
