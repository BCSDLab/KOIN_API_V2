package in.koreatech.koin.domain.order.cart.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin._common.code.ApiResponseCode;
import in.koreatech.koin._common.exception.CustomException;
import in.koreatech.koin.domain.order.cart.dto.CartAddItemCommand;
import in.koreatech.koin.domain.order.cart.dto.CartUpdateItemRequest;
import in.koreatech.koin.domain.order.cart.model.Cart;
import in.koreatech.koin.domain.order.cart.model.CartMenuItem;
import in.koreatech.koin.domain.order.cart.model.OrderableShopMenuOptions;
import in.koreatech.koin.domain.order.cart.model.OrderableShopMenus;
import in.koreatech.koin.domain.order.cart.repository.CartRepository;
import in.koreatech.koin.domain.order.shop.model.entity.menu.OrderableShopMenu;
import in.koreatech.koin.domain.order.shop.model.entity.menu.OrderableShopMenuOption;
import in.koreatech.koin.domain.order.shop.model.entity.menu.OrderableShopMenuPrice;
import in.koreatech.koin.domain.order.shop.model.entity.shop.OrderableShop;
import in.koreatech.koin.domain.order.shop.repository.OrderableShopRepository;
import in.koreatech.koin.domain.order.shop.repository.menu.OrderableShopMenuOptionRepository;
import in.koreatech.koin.domain.order.shop.repository.menu.OrderableShopMenuRepository;
import in.koreatech.koin.domain.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CartService {

    private final UserRepository userRepository;
    private final OrderableShopRepository orderableShopRepository;
    private final CartRepository cartRepository;
    private final OrderableShopMenuRepository orderableShopMenuRepository;
    private final OrderableShopMenuOptionRepository orderableShopMenuOptionRepository;
    private final EntityManager em;

    @Transactional
    public void addItem(CartAddItemCommand addItemCommand) {
        OrderableShop orderableShop = validateShopAndGetShop(addItemCommand.shopId());
        Cart cart = getOrCreateCart(addItemCommand.userId(), orderableShop);
        OrderableShopMenu menu = validateAndGetMenu(addItemCommand);
        List<OrderableShopMenuOption> selectedOptions = validateAndGetOptions(addItemCommand);

        cart.addItem(menu, menu.getMenuPriceById(addItemCommand.shopMenuPriceId()), selectedOptions);
        em.persist(cart);
    }

    @Transactional
    public void updateItemQuantity(Integer userId, Integer cartMenuItemID, Integer quantity) {
        Cart cart = getCartOrThrow(userId);
        cart.updateItemQuantity(cartMenuItemID, quantity);
    }

    @Transactional
    public void deleteItem(Integer userId, Integer cartMenuItemID) {
        Cart cart = getCartOrThrow(userId);
        cart.removeItem(cartMenuItemID);

        if (cart.getCartMenuItems().isEmpty()) {
            cartRepository.deleteByUserId(userId);
        }
    }

    @Transactional
    public void updateItem(CartUpdateItemRequest request, Integer cartMenuItemId, Integer userId) {
        Cart cart = getCartOrThrow(userId);
        CartMenuItem itemToUpdate = cart.getCartMenuItem(cartMenuItemId);

        // 새로운 가격 및 옵션 구성의 유효성 검증
        OrderableShopMenu menu = itemToUpdate.getOrderableShopMenu();
        Integer originalQuantity = itemToUpdate.getQuantity();

        menu.requiredMenuPriceById(request.orderableShopMenuPriceId());
        OrderableShopMenuPrice newPrice = menu.getMenuPriceById(request.orderableShopMenuPriceId());

        OrderableShopMenuOptions shopMenuOptions = orderableShopMenuOptionRepository.getAllByMenuId(menu.getId());
        List<OrderableShopMenuOption> newSelectedOptions = shopMenuOptions.resolveSelectedOptions(request.toOptions());

        // 변경 후의 구성이 장바구니에 이미 존재 하는지 확인
        Optional<CartMenuItem> existingSameItem = cart.findSameItem(menu, newPrice, newSelectedOptions, itemToUpdate.getId());

        if (existingSameItem.isPresent()) {
            // 장바구니에 동일한 구성한 상품 존재시 수량을 합치고 기존 상품 삭제
            existingSameItem.get().increaseQuantity(originalQuantity);
            cart.removeItem(itemToUpdate.getId());
        } else {
            // 동일 구성 상품 없으면 현재 상품의 구성을 직접 변경
            itemToUpdate.updatePriceAndOptions(newPrice, newSelectedOptions);
        }
    }

    @Transactional
    public void resetCart(Integer userId) {
        getCartOrThrow(userId);
        cartRepository.deleteByUserId(userId);
    }

    /**
     * 상점이 영업중 인지 확인 하고 도매인 엔티티 반환
     *
     * @param shopId 주문 가능 상점 ID
     * @throws CustomException (ApiResponseCode.NOT_FOUND_ORDERABLE_SHOP) 주문 가능 상점이 존재 하지 않은 경우
     * @throws CustomException (ApiResponseCode.SHOP_CLOSED) 상점의 영업 시간이 아닌 경우
     */
    private OrderableShop validateShopAndGetShop(Integer shopId) {
        OrderableShop orderableShop = orderableShopRepository.getById(shopId);
        orderableShop.requireShopOpen();
        return orderableShop;
    }

    /**
     * 장바구니 추가 요청 상품이 현재 장바구니에 담긴 상품과 동일한 상점의 상품인지 확인하고
     * 도메인 엔티티 반환 (장바구니가 존재 하지 않다면 엔티티 생성)
     *
     * @param userId 사용자 ID
     * @param orderableShop 주문 가능 상점 엔티티
     * @throws CustomException (ApiResponseCode.DIFFERENT_SHOP_ITEM_IN_CART) 추가 요청 상품이 담긴 상품의 상점과 일치 하지 않는 경우
     */
    private Cart getOrCreateCart(Integer userId, OrderableShop orderableShop) {
        Cart cart = cartRepository.findCartByUserId(userId).orElse(
            Cart.from(userRepository.getById(userId), orderableShop)
        );
        cart.validateSameShop(orderableShop.getId());
        return cart;
    }

    /**
     * 장바구니 도메인 엔티티 반환
     *
     * @param userId 사용자 ID
     * @throws CustomException (ApiResponseCode.NOT_FOUND_CART) 장바구니가 존재하지 않는 경우
     */
    private Cart getCartOrThrow(Integer userId) {
        return cartRepository.findCartByUserId(userId)
            .orElseThrow(() -> CustomException.of(ApiResponseCode.NOT_FOUND_CART));
    }

    /**
     * 클라이언트에서 전송된 메뉴 ID와 가격 옵션 ID가 실제 해당 상점에 속한 유효한 데이터인지 검증하고 도메인 엔티티 반환
     *
     * @param command 장바구니 상품 추가 요청 DTO
     * @throws CustomException (ApiResponseCode.NOT_FOUND_ORDERABLE_SHOP_MENU) 해당 메뉴를 찾을 수 없는 경우
     * @throws CustomException (ApiResponseCode.INVALID_MENU_IN_SHOP) 해당 메뉴가 상점에 속해 있지 않은 경우
     * @throws CustomException (ApiResponseCode.MENU_SOLD_OUT) 해당 메뉴가 품절 상태인 경우
     * @throws CustomException (ApiResponseCode.NOT_FOUND_ORDERABLE_SHOP_MENU_PRICE) 해당 메뉴의 가격 옵션이 잘못된 경우
     */
    private OrderableShopMenu validateAndGetMenu(CartAddItemCommand command) {
        OrderableShopMenus shopOrderableShopMenus = orderableShopMenuRepository.getAllByOrderableShopId(command.shopId());
        OrderableShopMenu menu = shopOrderableShopMenus.resolveSelectedMenu(command.shopMenuId());

        menu.validateSoldOut();
        menu.requiredMenuPriceById(command.shopMenuPriceId());

        return menu;
    }

    /**
     * 요청된 메뉴 옵션들의 유효성을 검증하고 도메인 엔티티를 반환.
     *
     * 클라이언트에서 전송된 옵션 데이터가 해당 메뉴에 속한 유효한 옵션인지,
     * 옵션 그룹의 선택 조건을 만족하는지 검증.
     * 1. 선택한 메뉴 옵션이 해당 옵션 그룹에 실제로 포함되어 있는지 확인
     * 2. 선택한 메뉴 옵션이 해당 메뉴에 속한 유효한 옵션인지 확인
     * 3. 해당 메뉴의 필수 옵션이 모두 선택되었는지 검증
     * 4. 각 옵션 그룹의 최소/최대 선택 조건(minSelect, maxSelect)을 만족하는지 검증
     * 5. 모든 검증을 통과한 경우에만 유효한 옵션 엔티티들을 반환
     *
     * @param command 장바구니 상품 추가 요청 DTO
     * @return 검증된 메뉴 옵션 엔티티 리스트
     * @throws CustomException 옵션 검증 실패 (잘못된 옵션, 필수 옵션 누락, 선택 조건 위반)
     */
    private List<OrderableShopMenuOption> validateAndGetOptions(CartAddItemCommand command) {
        OrderableShopMenuOptions shopMenuOptions = orderableShopMenuOptionRepository.getAllByMenuId(command.shopMenuId());
        return shopMenuOptions.resolveSelectedOptions(command.options());
    }
}
