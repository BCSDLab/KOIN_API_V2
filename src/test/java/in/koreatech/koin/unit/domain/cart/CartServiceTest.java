package in.koreatech.koin.unit.domain.cart;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import in.koreatech.koin._common.code.ApiResponseCode;
import in.koreatech.koin._common.exception.CustomException;
import in.koreatech.koin.domain.order.cart.dto.CartAddItemCommand;
import in.koreatech.koin.domain.order.cart.dto.CartUpdateItemRequest;
import in.koreatech.koin.domain.order.cart.model.Cart;
import in.koreatech.koin.domain.order.cart.model.CartMenuItem;
import in.koreatech.koin.domain.order.cart.model.OrderableShopMenuOptions;
import in.koreatech.koin.domain.order.cart.model.OrderableShopMenus;
import in.koreatech.koin.domain.order.cart.repository.CartRepository;
import in.koreatech.koin.domain.order.cart.service.CartService;
import in.koreatech.koin.domain.order.shop.model.entity.menu.OrderableShopMenu;
import in.koreatech.koin.domain.order.shop.model.entity.menu.OrderableShopMenuOption;
import in.koreatech.koin.domain.order.shop.model.entity.menu.OrderableShopMenuOptionGroup;
import in.koreatech.koin.domain.order.shop.model.entity.menu.OrderableShopMenuOptionGroupMap;
import in.koreatech.koin.domain.order.shop.model.entity.menu.OrderableShopMenuPrice;
import in.koreatech.koin.domain.order.shop.model.entity.shop.OrderableShop;
import in.koreatech.koin.domain.order.shop.repository.OrderableShopRepository;
import in.koreatech.koin.domain.order.shop.repository.menu.OrderableShopMenuOptionRepository;
import in.koreatech.koin.domain.order.shop.repository.menu.OrderableShopMenuRepository;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.repository.UserRepository;
import in.koreatech.koin.unit.fixutre.CartFixture;
import in.koreatech.koin.unit.fixutre.OrderableShopFixture;
import in.koreatech.koin.unit.fixutre.OrderableShopMenuFixture;
import in.koreatech.koin.unit.fixutre.UserFixture;
import jakarta.persistence.EntityManager;

@ExtendWith(MockitoExtension.class)
public class CartServiceTest {

    @InjectMocks
    private CartService cartService;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private OrderableShopMenuOptionRepository orderableShopMenuOptionRepository;

    @Mock
    private OrderableShopRepository orderableShopRepository;

    @Mock
    private OrderableShopMenuRepository orderableShopMenuRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EntityManager em;

    private User user;
    private OrderableShop orderableShop;
    private OrderableShopMenu menuA;
    private OrderableShopMenu menuB;
    private OrderableShopMenu menuSoldOut;
    private OrderableShopMenuPrice menuPriceA;
    private OrderableShopMenuPrice menuPriceB;
    private OrderableShopMenuPrice menuPriceC;
    private OrderableShopMenuOption option1;
    private OrderableShopMenuOption option2;
    private OrderableShopMenuOption option3;
    private OrderableShopMenuOption option4;
    private OrderableShopMenuOption option5;
    private OrderableShopMenuOption option6;
    private OrderableShopMenuOption option7;
    private OrderableShopMenuOption option8;
    private OrderableShopMenuOptionGroup toppingGroup;
    private OrderableShopMenuOptionGroup sourceGroup;
    private OrderableShopMenuOptionGroup alcoholGroup;
    private OrderableShopMenuOptionGroupMap optionGroupMap;
    private OrderableShopMenuOptionGroupMap optionGroupMapB;
    private OrderableShopMenuOptionGroupMap optionGroupMapC;
    private Cart cart;

    @BeforeEach
    void setUp() {
        user = UserFixture.코인_유저();
        ReflectionTestUtils.setField(user, "id", 1);

        orderableShop = OrderableShopFixture.김밥천국();
        ReflectionTestUtils.setField(orderableShop, "id", 101);

        menuA = OrderableShopMenuFixture.createMenu(orderableShop, "김밥", 201);
        menuB = OrderableShopMenuFixture.createMenu(orderableShop, "라면", 202);
        menuSoldOut = OrderableShopMenuFixture.createSoldOutMenu(orderableShop, "떡볶이", 203);

        menuPriceA = OrderableShopMenuFixture.createMenuPrice(menuA, "소고기 김밥", 6000, 301);
        menuPriceB = OrderableShopMenuFixture.createMenuPrice(menuA, "참치 김밥", 5500, 302);
        ReflectionTestUtils.setField(menuA, "menuPrices", List.of(menuPriceA, menuPriceB));

        menuPriceC = OrderableShopMenuFixture.createMenuPrice(menuB, "신라면", 5500, 303);
        ReflectionTestUtils.setField(menuB, "menuPrices", List.of(menuPriceC));

        toppingGroup = OrderableShopMenuFixture.createMenuOptionGroupWithEmptyMenuOption(
            orderableShop, "토핑 추가", 0, 2, false, 401
        );

        sourceGroup = OrderableShopMenuFixture.createMenuOptionGroupWithEmptyMenuOption(
            orderableShop, "소스 추가", 1, 2, true, 402
        );

        alcoholGroup = OrderableShopMenuFixture.createMenuOptionGroupWithEmptyMenuOption(
            orderableShop, "술 추가", 2, 3, false, 403
        );

        option1 = OrderableShopMenuFixture.createMenuOption(toppingGroup, "치즈 추가", 500, 501);
        option2 = OrderableShopMenuFixture.createMenuOption(toppingGroup, "단무지 추가",300, 502);
        option3 = OrderableShopMenuFixture.createMenuOption(sourceGroup, "캡사이신 추가",300, 503);
        option4 = OrderableShopMenuFixture.createMenuOption(sourceGroup, "겨자 추가",300, 504);
        option5 = OrderableShopMenuFixture.createMenuOption(alcoholGroup, "참이슬",4500, 505);
        option6 = OrderableShopMenuFixture.createMenuOption(alcoholGroup, "테라",2500, 506);
        option7 = OrderableShopMenuFixture.createMenuOption(alcoholGroup, "처음처럼",2500, 507);
        option8 = OrderableShopMenuFixture.createMenuOption(alcoholGroup, "카스",2500, 508);

        ReflectionTestUtils.setField(toppingGroup, "menuOptions", List.of(option1, option2));
        ReflectionTestUtils.setField(sourceGroup, "menuOptions", List.of(option3, option4));
        ReflectionTestUtils.setField(alcoholGroup, "menuOptions", List.of(option5, option6, option7, option8));

        optionGroupMap = OrderableShopMenuFixture.createMenuOptionGroupMap(toppingGroup, menuA, 601);
        ReflectionTestUtils.setField(menuA, "menuOptionGroupMap", List.of(optionGroupMap));

        optionGroupMapB = OrderableShopMenuFixture.createMenuOptionGroupMap(sourceGroup, menuB, 602);
        ReflectionTestUtils.setField(menuB, "menuOptionGroupMap", List.of(optionGroupMapB));

        optionGroupMapC = OrderableShopMenuFixture.createMenuOptionGroupMap(alcoholGroup, menuB, 603);
        ReflectionTestUtils.setField(menuB, "menuOptionGroupMap", List.of(optionGroupMapC));

        cart = CartFixture.createCart(user, orderableShop);
    }

    @Test
    @DisplayName("장바구니 상품 추가 성공")
    void addItem_Success() {
        when(orderableShopRepository.getById(orderableShop.getId())).thenReturn(orderableShop);
        when(cartRepository.findCartByUserId(user.getId())).thenReturn(Optional.of(cart));
        when(userRepository.getById(user.getId())).thenReturn(user);

        OrderableShopMenus orderableShopMenus = new OrderableShopMenus(orderableShop.getId(), List.of(menuA));
        when(orderableShopMenuRepository.getAllByOrderableShopId(orderableShop.getId())).thenReturn(orderableShopMenus);

        OrderableShopMenuOptions menuOptions = new OrderableShopMenuOptions(List.of(option1, option2));
        when(orderableShopMenuOptionRepository.getAllByMenuId(menuA.getId())).thenReturn(menuOptions);

        CartAddItemCommand command = new CartAddItemCommand(
            user.getId(), orderableShop.getId(), menuA.getId(), menuPriceA.getId(), List.of()
        );

        // When
        cartService.addItem(command);

        // Then
        verify(em).persist(cart);
        assertThat(cart.getCartMenuItems()).hasSize(1);

        CartMenuItem existItem = cart.getCartMenuItems().get(0);
        assertThat(existItem.getOrderableShopMenu().getId()).isEqualTo(menuA.getId());
    }

    @Test
    @DisplayName("장바구니 상품 추가 실패 - 상점이 영업중이지 않은 경우")
    void addItem_Fail_ShopClosed() {
        // Given
        OrderableShop orderableShopA = OrderableShopFixture.김밥천국_영업_안함();
        when(orderableShopRepository.getById(orderableShopA.getId())).thenReturn(orderableShopA);

        CartAddItemCommand command = new CartAddItemCommand(
            user.getId(), orderableShopA.getId(), 1, 1, List.of()
        );

        // When & Then
        CustomException exception = assertThrows(CustomException.class, () -> {
            cartService.addItem(command);
        });

        assertThat(exception.getErrorCode()).isEqualTo(ApiResponseCode.SHOP_CLOSED);
        assertThat(exception.getMessage()).isEqualTo("상점의 영업시간이 아닙니다.");
    }

    @Test
    @DisplayName("장바구니 상품 추가 실패 - 다른 상점의 상품이 이미 장바구니에 담겨있는 경우")
    void addItem_Fail_DifferentShopItemInCart() {
        // Given
        // 새로 추가하려는 상점 (다른 상점)
        OrderableShop newShop = OrderableShopFixture.마슬랜();

        Cart existingCart = CartFixture.createCart(user, orderableShop);

        when(orderableShopRepository.getById(newShop.getId())).thenReturn(newShop);
        when(cartRepository.findCartByUserId(user.getId())).thenReturn(Optional.of(existingCart));
        when(userRepository.getById(user.getId())).thenReturn(user);

        CartAddItemCommand command = new CartAddItemCommand(
            user.getId(), newShop.getId(), 1, 1, List.of()
        );

        // When & Then
        CustomException exception = assertThrows(CustomException.class, () -> {
            cartService.addItem(command);
        });

        assertThat(exception.getErrorCode()).isEqualTo(ApiResponseCode.DIFFERENT_SHOP_ITEM_IN_CART);
        assertThat(exception.getMessage()).isEqualTo("장바구니에는 동일한 상점의 상품만 담을 수 있습니다.");
    }

    @Test
    @DisplayName("장바구니 상품 추가 실패 - 해당 메뉴가 존재하지 않는 경우")
    void addItem_Fail_NotFoundMenu() {
        // Given
        Cart existingCart = CartFixture.createCart(user, orderableShop);

        when(orderableShopRepository.getById(orderableShop.getId())).thenReturn(orderableShop);
        when(cartRepository.findCartByUserId(user.getId())).thenReturn(Optional.of(existingCart));
        when(userRepository.getById(user.getId())).thenReturn(user);

        when(orderableShopMenuRepository.getAllByOrderableShopId(orderableShop.getId()))
            .thenThrow(CustomException.of(
                ApiResponseCode.NOT_FOUND_ORDERABLE_SHOP_MENU,
                "해당 상점에 메뉴가 존재하지 않습니다 : " + orderableShop.getId()
            ));

        CartAddItemCommand command = new CartAddItemCommand(
            user.getId(), orderableShop.getId(), 1, 1, List.of()
        );

        // When & Then
        CustomException exception = assertThrows(CustomException.class, () -> {
            cartService.addItem(command);
        });

        assertThat(exception.getErrorCode()).isEqualTo(ApiResponseCode.NOT_FOUND_ORDERABLE_SHOP_MENU);
        assertThat(exception.getDetail()).contains("메뉴가 존재하지 않습니다 : 1");
    }

    @Test
    @DisplayName("장바구니 상품 추가 실패 - 해당 메뉴가 상점에 속해 있지 않은 경우")
    void addItem_Fail_InvalidMenu() {
        // Given
        // 장바구니에 이미 담겨있는 상점 (김밥천국)
        OrderableShop existingShop = OrderableShopFixture.김밥천국();

        Cart existingCart = CartFixture.createCart(user, existingShop);

        when(orderableShopRepository.getById(existingShop.getId())).thenReturn(existingShop);
        when(cartRepository.findCartByUserId(user.getId())).thenReturn(Optional.of(existingCart));
        when(userRepository.getById(user.getId())).thenReturn(user);

        OrderableShopMenus orderableShopMenus = new OrderableShopMenus(existingShop.getId(), List.of(menuA));
        when(orderableShopMenuRepository.getAllByOrderableShopId(existingShop.getId())).thenReturn(orderableShopMenus);

        CartAddItemCommand command = new CartAddItemCommand(
            user.getId(), existingShop.getId(), 99, 1, List.of()
        );

        // When & Then
        CustomException exception = assertThrows(CustomException.class, () -> {
            cartService.addItem(command);
        });

        assertThat(exception.getErrorCode()).isEqualTo(ApiResponseCode.INVALID_MENU_IN_SHOP);
        assertThat(exception.getMessage()).isEqualTo("선택한 메뉴는 해당 상점에 속해있지 않습니다");
    }

    @Test
    @DisplayName("장바구니 상품 추가 실패 - 해당 메뉴가 품절 상태인 경우")
    void addItem_Fail_SoldOutMenu() {
        // Given
        Cart existingCart = CartFixture.createCart(user, orderableShop);

        when(orderableShopRepository.getById(orderableShop.getId())).thenReturn(orderableShop);
        when(cartRepository.findCartByUserId(user.getId())).thenReturn(Optional.of(existingCart));
        when(userRepository.getById(user.getId())).thenReturn(user);

        OrderableShopMenus orderableShopMenus = new OrderableShopMenus(orderableShop.getId(), List.of(menuSoldOut));
        when(orderableShopMenuRepository.getAllByOrderableShopId(orderableShop.getId())).thenReturn(orderableShopMenus);

        CartAddItemCommand command = new CartAddItemCommand(
            user.getId(), orderableShop.getId(), menuSoldOut.getId(), 1, List.of()
        );

        // When & Then
        CustomException exception = assertThrows(CustomException.class, () -> {
            cartService.addItem(command);
        });

        assertThat(exception.getErrorCode()).isEqualTo(ApiResponseCode.MENU_SOLD_OUT);
        assertThat(exception.getMessage()).isEqualTo("상품이 매진되었습니다");
    }

    @Test
    @DisplayName("장바구니 상품 추가 실패 - 해당 메뉴의 가격 옵션이 잘못된 경우")
    void addItem_Fail_MenuPriceNotFound() {
        // Given
        Cart existingCart = CartFixture.createCart(user, orderableShop);

        when(orderableShopRepository.getById(orderableShop.getId())).thenReturn(orderableShop);
        when(cartRepository.findCartByUserId(user.getId())).thenReturn(Optional.of(existingCart));
        when(userRepository.getById(user.getId())).thenReturn(user);

        OrderableShopMenus orderableShopMenus = new OrderableShopMenus(orderableShop.getId(), List.of(menuA));
        when(orderableShopMenuRepository.getAllByOrderableShopId(orderableShop.getId())).thenReturn(orderableShopMenus);

        CartAddItemCommand command = new CartAddItemCommand(
            user.getId(), orderableShop.getId(), menuA.getId(), menuPriceC.getId(), List.of()
        );

        // When & Then
        CustomException exception = assertThrows(CustomException.class, () -> {
            cartService.addItem(command);
        });

        assertThat(exception.getErrorCode()).isEqualTo(ApiResponseCode.NOT_FOUND_ORDERABLE_SHOP_MENU_PRICE);
        assertThat(exception.getMessage()).isEqualTo("유효하지 않은 가격 ID 입니다.");
    }

    @Test
    @DisplayName("장바구니 상품 추가 실패 - 필수 옵션 그룹이 선택되지 않은 경우")
    void addItem_Fail_RequiredOptionGroupMissing() {
        // Given
        when(orderableShopRepository.getById(orderableShop.getId())).thenReturn(orderableShop);
        when(cartRepository.findCartByUserId(user.getId())).thenReturn(Optional.empty());
        when(userRepository.getById(user.getId())).thenReturn(user);

        OrderableShopMenus orderableShopMenus = new OrderableShopMenus(orderableShop.getId(), List.of(menuB));
        when(orderableShopMenuRepository.getAllByOrderableShopId(orderableShop.getId())).thenReturn(orderableShopMenus);

        OrderableShopMenuOptions menuOptions = new OrderableShopMenuOptions(List.of(option3, option4, option5, option6, option7, option8));
        when(orderableShopMenuOptionRepository.getAllByMenuId(menuB.getId())).thenReturn(menuOptions);

        CartAddItemCommand command = new CartAddItemCommand(
            user.getId(), orderableShop.getId(), menuB.getId(), menuPriceC.getId(), List.of()
        );

        // When & Then
        CustomException exception = assertThrows(CustomException.class, () -> {
            cartService.addItem(command);
        });

        assertThat(exception.getErrorCode()).isEqualTo(ApiResponseCode.REQUIRED_OPTION_GROUP_MISSING);
        assertThat(exception.getMessage()).isEqualTo("필수 옵션 그룹을 선택하지 않았습니다.");
    }

    @Test
    @DisplayName("장바구니 상품 추가 실패 - 해당 메뉴에 존재하지 않는 옵션 ID인 경우")
    void addItem_Fail_InvalidOption() {
        // Given
        when(orderableShopRepository.getById(orderableShop.getId())).thenReturn(orderableShop);
        when(cartRepository.findCartByUserId(user.getId())).thenReturn(Optional.empty());
        when(userRepository.getById(user.getId())).thenReturn(user);

        OrderableShopMenus orderableShopMenus = new OrderableShopMenus(orderableShop.getId(), List.of(menuA));
        when(orderableShopMenuRepository.getAllByOrderableShopId(orderableShop.getId())).thenReturn(orderableShopMenus);

        OrderableShopMenuOptions menuOptions = new OrderableShopMenuOptions(List.of(option1, option2));
        when(orderableShopMenuOptionRepository.getAllByMenuId(menuA.getId())).thenReturn(menuOptions);

        CartAddItemCommand command = new CartAddItemCommand(
            user.getId(), orderableShop.getId(), menuA.getId(), menuPriceA.getId(),
            List.of(new CartAddItemCommand.Option(toppingGroup.getId(), option3.getId()))
        );

        // When & Then
        CustomException exception = assertThrows(CustomException.class, () -> {
            cartService.addItem(command);
        });

        assertThat(exception.getErrorCode()).isEqualTo(ApiResponseCode.NOT_FOUND_ORDERABLE_SHOP_MENU_OPTION);
        assertThat(exception.getMessage()).isEqualTo("유효하지 않은 옵션 ID 입니다.");
    }

    @Test
    @DisplayName("장바구니 상품 추가 실패 - 요청한 옵션 그룹 ID가 실제 옵션의 그룹 ID와 일치하지 않는 경우")
    void addItem_Fail_InvalidOptionInGroup() {
        // Given
        when(orderableShopRepository.getById(orderableShop.getId())).thenReturn(orderableShop);
        when(cartRepository.findCartByUserId(user.getId())).thenReturn(Optional.empty());
        when(userRepository.getById(user.getId())).thenReturn(user);

        OrderableShopMenus orderableShopMenus = new OrderableShopMenus(orderableShop.getId(), List.of(menuA));
        when(orderableShopMenuRepository.getAllByOrderableShopId(orderableShop.getId())).thenReturn(orderableShopMenus);

        OrderableShopMenuOptions menuOptions = new OrderableShopMenuOptions(List.of(option1, option2));
        when(orderableShopMenuOptionRepository.getAllByMenuId(menuA.getId())).thenReturn(menuOptions);

        CartAddItemCommand command = new CartAddItemCommand(
            user.getId(), orderableShop.getId(), menuA.getId(), menuPriceA.getId(),
            List.of(new CartAddItemCommand.Option(sourceGroup.getId(), option1.getId()))
        );

        // When & Then
        CustomException exception = assertThrows(CustomException.class, () -> {
            cartService.addItem(command);
        });

        assertThat(exception.getErrorCode()).isEqualTo(ApiResponseCode.INVALID_OPTION_IN_GROUP);
        assertThat(exception.getMessage()).isEqualTo("선택한 옵션이 해당 옵션 그룹에 속해있지 않습니다.");
    }

    @Test
    @DisplayName("장바구니 상품 추가 실패 - 옵션 그룹의 최소 선택 개수를 만족하지 못한 경우")
    void addItem_Fail_MinSelectionNotMet() {
        // Given
        when(orderableShopRepository.getById(orderableShop.getId())).thenReturn(orderableShop);
        when(cartRepository.findCartByUserId(user.getId())).thenReturn(Optional.empty());
        when(userRepository.getById(user.getId())).thenReturn(user);

        OrderableShopMenus orderableShopMenus = new OrderableShopMenus(orderableShop.getId(), List.of(menuB));
        when(orderableShopMenuRepository.getAllByOrderableShopId(orderableShop.getId())).thenReturn(orderableShopMenus);

        OrderableShopMenuOptions menuOptions = new OrderableShopMenuOptions(List.of(option3, option4, option5, option6, option7, option8));
        when(orderableShopMenuOptionRepository.getAllByMenuId(menuB.getId())).thenReturn(menuOptions);

        CartAddItemCommand command = new CartAddItemCommand(
            user.getId(), orderableShop.getId(), menuB.getId(), menuPriceC.getId(),
            List.of(
                new CartAddItemCommand.Option(sourceGroup.getId(), option3.getId()),
                new CartAddItemCommand.Option(alcoholGroup.getId(), option5.getId())
            )
        );

        // When & Then
        CustomException exception = assertThrows(CustomException.class, () -> {
            cartService.addItem(command);
        });

        assertThat(exception.getErrorCode()).isEqualTo(ApiResponseCode.MIN_SELECTION_NOT_MET);
        assertThat(exception.getMessage()).isEqualTo("옵션 그룹의 최소 선택 개수를 만족하지 못했습니다.");
    }

    @Test
    @DisplayName("장바구니 상품 추가 실패 - 옵션 그룹의 최대 선택 개수를 초과한 경우")
    void addItem_Fail_MaxSelectionExceeded() {
        // Given
        when(orderableShopRepository.getById(orderableShop.getId())).thenReturn(orderableShop);
        when(cartRepository.findCartByUserId(user.getId())).thenReturn(Optional.empty());
        when(userRepository.getById(user.getId())).thenReturn(user);

        OrderableShopMenus orderableShopMenus = new OrderableShopMenus(orderableShop.getId(), List.of(menuB));
        when(orderableShopMenuRepository.getAllByOrderableShopId(orderableShop.getId())).thenReturn(orderableShopMenus);

        OrderableShopMenuOptions menuOptions = new OrderableShopMenuOptions(List.of(option3, option4, option5, option6, option7, option8));
        when(orderableShopMenuOptionRepository.getAllByMenuId(menuB.getId())).thenReturn(menuOptions);

        CartAddItemCommand command = new CartAddItemCommand(
            user.getId(), orderableShop.getId(), menuB.getId(), menuPriceC.getId(),
            List.of(new CartAddItemCommand.Option(sourceGroup.getId(), option3.getId()),
                new CartAddItemCommand.Option(alcoholGroup.getId(), option5.getId()),
                new CartAddItemCommand.Option(alcoholGroup.getId(), option6.getId()),
                new CartAddItemCommand.Option(alcoholGroup.getId(), option7.getId()),
                new CartAddItemCommand.Option(alcoholGroup.getId(), option8.getId())
            )
        );

        // When & Then
        CustomException exception = assertThrows(CustomException.class, () -> {
            cartService.addItem(command);
        });

        assertThat(exception.getErrorCode()).isEqualTo(ApiResponseCode.MAX_SELECTION_EXCEEDED);
        assertThat(exception.getMessage()).isEqualTo("옵션 그룹의 최대 선택 개수를 초과했습니다.");
    }

    @Test
    @DisplayName("장바구니 상품 수정 성공 - 장바구니에 동일 구성 상품이 없어 기존 아이템이 직접 수정되는 경우")
    void updateItem_Success_UpdateCartItemDirectly() {
        // Given
        CartMenuItem itemToUpdate = CartFixture.cartMenuItem(cart, menuA, menuPriceA, List.of(option1), 1);
        ReflectionTestUtils.setField(itemToUpdate, "id", 7);
        ReflectionTestUtils.setField(cart, "cartMenuItems", new ArrayList<>(List.of(itemToUpdate)));

        when(cartRepository.findCartByUserId(user.getId())).thenReturn(Optional.of(cart));

        OrderableShopMenuOptions menuOptions = new OrderableShopMenuOptions(List.of(option1, option2));
        when(orderableShopMenuOptionRepository.getAllByMenuId(menuA.getId())).thenReturn(menuOptions);

        // 장바구니에 담긴 상품의 가격 옵션과 선택 옵션을 모두 변경 - 모두 유효한 값으로 변경
        CartUpdateItemRequest request = new CartUpdateItemRequest(
            menuPriceB.getId(),
            List.of(new CartUpdateItemRequest.InnerOptionRequest(toppingGroup.getId(), option2.getId()))
        );

        // When
        cartService.updateItem(request, itemToUpdate.getId(), user.getId());

        // Then
        assertThat(cart.getCartMenuItems()).hasSize(1);

        CartMenuItem updatedItem = cart.getCartMenuItems().get(0);

        assertThat(updatedItem.getOrderableShopMenuPrice().getId()).isEqualTo(menuPriceB.getId());
        assertThat(updatedItem.getIsModified()).isTrue();
        assertThat(updatedItem.getCartMenuItemOptions()).hasSize(1);
        assertThat(updatedItem.getCartMenuItemOptions().get(0).getOrderableShopMenuOption().getId()).isEqualTo(option2.getId());
    }

    @Test
    @DisplayName("장바구니 상품 수정 성공 - 장바구니에 동일 구성 상품이 있어 해당 상품의 수량이 증가되고 기존 상품은 삭제되는 경우")
    void updateItem_Success_UpdateCartItemMerged() {
        // Given
        // 장바구니에 상품 2개 미리 담기. menuA - 수정 대상, menuB - 병합 대상
        CartMenuItem itemToUpdate = CartFixture.cartMenuItem(cart, menuA, menuPriceA, List.of(option1), 1);
        ReflectionTestUtils.setField(itemToUpdate, "id", 777);

        CartMenuItem existingItem = CartFixture.cartMenuItem(cart, menuA, menuPriceB, List.of(option2), 3);
        ReflectionTestUtils.setField(existingItem, "id", 888);

        ReflectionTestUtils.setField(cart, "cartMenuItems", new ArrayList<>(List.of(itemToUpdate, existingItem)));

        when(cartRepository.findCartByUserId(user.getId())).thenReturn(Optional.of(cart));

        OrderableShopMenuOptions menuOptions = new OrderableShopMenuOptions(List.of(option1, option2));
        when(orderableShopMenuOptionRepository.getAllByMenuId(menuA.getId())).thenReturn(menuOptions);

        // 장바구니에 담긴 상품(itemToUpdate)의 가격 옵션과 선택 옵션을 existingItem 과 동일하게 변경
        CartUpdateItemRequest request = new CartUpdateItemRequest(
            menuPriceB.getId(),
            List.of(new CartUpdateItemRequest.InnerOptionRequest(toppingGroup.getId(), option2.getId()))
        );

        // When
        cartService.updateItem(request, itemToUpdate.getId(), user.getId());

        // Then
        assertThat(cart.getCartMenuItems()).hasSize(1);

        CartMenuItem mergedItem = cart.getCartMenuItems().get(0);

        // itemToUpdate은 없어지고 existingItem의 수량이 1 증가해야 함
        assertThat(mergedItem.getId()).isEqualTo(existingItem.getId());
        assertThat(mergedItem.getQuantity()).isEqualTo(4);
    }

    @Test
    @DisplayName("장바구니 상품 수정 실패 - 수정 시도한 가격 옵션이 해당 메뉴에 속해 있지 않은 경우")
    void updateItem_Fail_InvalidMenuPriceOption() {
        // Given
        CartMenuItem itemA = CartFixture.cartMenuItem(cart, menuA, menuPriceA, List.of(option1), 1);
        ReflectionTestUtils.setField(itemA, "id", 777);
        ReflectionTestUtils.setField(cart, "cartMenuItems", new ArrayList<>(List.of(itemA)));

        when(cartRepository.findCartByUserId(user.getId())).thenReturn(Optional.of(cart));

        // 장바구니에 담긴 상품의 가격 옵션을 유효하지 않은 ID로 변경 시도
        CartUpdateItemRequest request = new CartUpdateItemRequest(
            menuPriceC.getId(),
            List.of(new CartUpdateItemRequest.InnerOptionRequest(sourceGroup.getId(), option3.getId()))
        );

        // When & Then
        CustomException exception = assertThrows(CustomException.class, () -> {
            cartService.updateItem(request, itemA.getId(), user.getId());;
        });

        assertThat(exception.getErrorCode()).isEqualTo(ApiResponseCode.NOT_FOUND_ORDERABLE_SHOP_MENU_PRICE);
        assertThat(exception.getMessage()).isEqualTo("유효하지 않은 가격 ID 입니다.");
    }

    @Test
    @DisplayName("장바구니 상품 수정 실패 - 필수적으로 선택되어야 하는 옵션 그룹이 선택되지 않은 경우")
    void updateItem_Fail_RequiredOptionGroupSelectedMissing() {
        // Given
        CartMenuItem itemA = CartFixture.cartMenuItem(cart, menuB, menuPriceC, List.of(option3), 1);
        ReflectionTestUtils.setField(itemA, "id", 777);
        ReflectionTestUtils.setField(cart, "cartMenuItems", new ArrayList<>(List.of(itemA)));

        when(cartRepository.findCartByUserId(user.getId())).thenReturn(Optional.of(cart));

        OrderableShopMenuOptions menuOptions = new OrderableShopMenuOptions(List.of(option3));
        when(orderableShopMenuOptionRepository.getAllByMenuId(menuB.getId())).thenReturn(menuOptions);

        // 필수 옵션 그룹 선택을 누락한 요청 생성
        CartUpdateItemRequest request = new CartUpdateItemRequest(
            menuPriceC.getId(),
            List.of()
        );

        // When & Then
        CustomException exception = assertThrows(CustomException.class, () -> {
            cartService.updateItem(request, itemA.getId(), user.getId());;
        });

        assertThat(exception.getErrorCode()).isEqualTo(ApiResponseCode.REQUIRED_OPTION_GROUP_MISSING);
        assertThat(exception.getMessage()).isEqualTo("필수 옵션 그룹을 선택하지 않았습니다.");
    }

    @Test
    @DisplayName("장바구니 상품 수정 실패 - 메뉴에 존재하지 않는 옵션 ID인 경우")
    void updateItem_Fail_InvalidOption() {
        // Given
        CartMenuItem itemA = CartFixture.cartMenuItem(cart, menuA, menuPriceA, List.of(option1), 1);
        ReflectionTestUtils.setField(itemA, "id", 777);
        ReflectionTestUtils.setField(cart, "cartMenuItems", new ArrayList<>(List.of(itemA)));

        when(cartRepository.findCartByUserId(user.getId())).thenReturn(Optional.of(cart));

        OrderableShopMenuOptions menuOptions = new OrderableShopMenuOptions(List.of(option1, option2));
        when(orderableShopMenuOptionRepository.getAllByMenuId(menuA.getId())).thenReturn(menuOptions);

        // 메뉴에 속하지 않는 옵션 으로 변경 하는 요청 생성
        CartUpdateItemRequest request = new CartUpdateItemRequest(
            menuPriceB.getId(),
            List.of(new CartUpdateItemRequest.InnerOptionRequest(toppingGroup.getId(), option3.getId()))
        );

        // When & Then
        CustomException exception = assertThrows(CustomException.class, () -> {
            cartService.updateItem(request, itemA.getId(), user.getId());;
        });

        assertThat(exception.getErrorCode()).isEqualTo(ApiResponseCode.NOT_FOUND_ORDERABLE_SHOP_MENU_OPTION);
        assertThat(exception.getMessage()).isEqualTo("유효하지 않은 옵션 ID 입니다.");
    }

    @Test
    @DisplayName("장바구니 상품 수정 실패 - 요청한 옵션 그룹 ID가 실제 해당 옵션의 옵션 그룹 ID와 일치 하지 않는 경우")
    void updateItem_Fail_InvalidOptionInGroup() {
        // Given
        CartMenuItem itemA = CartFixture.cartMenuItem(cart, menuB, menuPriceC, List.of(option3), 1);
        ReflectionTestUtils.setField(itemA, "id", 777);
        ReflectionTestUtils.setField(cart, "cartMenuItems", new ArrayList<>(List.of(itemA)));

        when(cartRepository.findCartByUserId(user.getId())).thenReturn(Optional.of(cart));

        OrderableShopMenuOptions menuOptions = new OrderableShopMenuOptions(List.of(option3, option4, option5, option6));
        when(orderableShopMenuOptionRepository.getAllByMenuId(menuB.getId())).thenReturn(menuOptions);

        // 옵션 그룹과 옵션 ID가 일치하지 않는 요청 생성
        CartUpdateItemRequest request = new CartUpdateItemRequest(
            menuPriceC.getId(),
            List.of(new CartUpdateItemRequest.InnerOptionRequest(alcoholGroup.getId(), option4.getId()))
        );

        // When & Then
        CustomException exception = assertThrows(CustomException.class, () -> {
            cartService.updateItem(request, itemA.getId(), user.getId());;
        });

        assertThat(exception.getErrorCode()).isEqualTo(ApiResponseCode.INVALID_OPTION_IN_GROUP);
        assertThat(exception.getMessage()).isEqualTo("선택한 옵션이 해당 옵션 그룹에 속해있지 않습니다.");
    }

    @Test
    @DisplayName("장바구니 상품 수정 실패 - 옵션 그룹의 최소 선택 개수를 만족하지 못한 경우")
    void updateItem_Fail_MaxSelectionNotMet() {
        // Given
        CartMenuItem itemA = CartFixture.cartMenuItem(cart, menuB, menuPriceC, List.of(option3), 1);
        ReflectionTestUtils.setField(itemA, "id", 777);
        ReflectionTestUtils.setField(cart, "cartMenuItems", new ArrayList<>(List.of(itemA)));

        when(cartRepository.findCartByUserId(user.getId())).thenReturn(Optional.of(cart));

        OrderableShopMenuOptions menuOptions = new OrderableShopMenuOptions(
            List.of(option3, option4, option5, option6, option7, option8)
        );
        when(orderableShopMenuOptionRepository.getAllByMenuId(menuB.getId())).thenReturn(menuOptions);

        CartUpdateItemRequest request = new CartUpdateItemRequest(
            menuPriceC.getId(),
            List.of(new CartUpdateItemRequest.InnerOptionRequest(alcoholGroup.getId(), option5.getId()),
                new CartUpdateItemRequest.InnerOptionRequest(alcoholGroup.getId(), option6.getId()),
                new CartUpdateItemRequest.InnerOptionRequest(alcoholGroup.getId(), option7.getId()),
                new CartUpdateItemRequest.InnerOptionRequest(alcoholGroup.getId(), option8.getId()),
                new CartUpdateItemRequest.InnerOptionRequest(sourceGroup.getId(), option3.getId()))
        );

        // When & Then
        CustomException exception = assertThrows(CustomException.class, () -> {
            cartService.updateItem(request, itemA.getId(), user.getId());;
        });

        assertThat(exception.getErrorCode()).isEqualTo(ApiResponseCode.MAX_SELECTION_EXCEEDED);
        assertThat(exception.getMessage()).isEqualTo("옵션 그룹의 최대 선택 개수를 초과했습니다.");
    }

    @Test
    @DisplayName("장바구니 상품 수정 실패 - 옵션 그룹의 최대 선택 개수를 만족하지 못한 경우")
    void updateItem_Fail_MinSelectionNotMet() {
        // Given
        CartMenuItem itemA = CartFixture.cartMenuItem(cart, menuB, menuPriceC, List.of(option3), 1);
        ReflectionTestUtils.setField(itemA, "id", 777);
        ReflectionTestUtils.setField(cart, "cartMenuItems", new ArrayList<>(List.of(itemA)));

        when(cartRepository.findCartByUserId(user.getId())).thenReturn(Optional.of(cart));

        OrderableShopMenuOptions menuOptions = new OrderableShopMenuOptions(List.of(option3, option4, option5, option6));
        when(orderableShopMenuOptionRepository.getAllByMenuId(menuB.getId())).thenReturn(menuOptions);

        CartUpdateItemRequest request = new CartUpdateItemRequest(
            menuPriceC.getId(),
            List.of(new CartUpdateItemRequest.InnerOptionRequest(alcoholGroup.getId(), option5.getId()),
                new CartUpdateItemRequest.InnerOptionRequest(sourceGroup.getId(), option3.getId()))
        );

        // When & Then
        CustomException exception = assertThrows(CustomException.class, () -> {
            cartService.updateItem(request, itemA.getId(), user.getId());;
        });

        assertThat(exception.getErrorCode()).isEqualTo(ApiResponseCode.MIN_SELECTION_NOT_MET);
        assertThat(exception.getMessage()).isEqualTo("옵션 그룹의 최소 선택 개수를 만족하지 못했습니다.");
    }

    @Test
    @DisplayName("장바구니 상품 수량 변경 성공 - 정상적인 수량으로 변경하는 경우")
    void updateItemQuantity_Success() {
        // Given
        CartMenuItem itemToUpdate = CartFixture.cartMenuItem(cart, menuA, menuPriceA, List.of(option1), 7);
        ReflectionTestUtils.setField(itemToUpdate, "id", 7);
        ReflectionTestUtils.setField(cart, "cartMenuItems", new ArrayList<>(List.of(itemToUpdate)));

        when(cartRepository.findCartByUserId(user.getId())).thenReturn(Optional.of(cart));

        // When
        cartService.updateItemQuantity(user.getId(), itemToUpdate.getId(), 9999);

        // Then
        assertThat(cart.getCartMenuItems()).hasSize(1);

        CartMenuItem quantityUpdatedItem = cart.getCartMenuItems().get(0);

        assertThat(quantityUpdatedItem.getId()).isEqualTo(itemToUpdate.getId());
        assertThat(quantityUpdatedItem.getQuantity()).isEqualTo(9999);
    }

    @Test
    @DisplayName("장바구니 상품 수량 변경 실패 - 수량이 Null인 경우")
    void updateItemQuantity_Fail_QuantityValueInvalid_Null() {
        // Given
        CartMenuItem itemToUpdate = CartFixture.cartMenuItem(cart, menuA, menuPriceA, List.of(option1), 7);
        ReflectionTestUtils.setField(itemToUpdate, "id", 7);
        ReflectionTestUtils.setField(cart, "cartMenuItems", new ArrayList<>(List.of(itemToUpdate)));

        when(cartRepository.findCartByUserId(user.getId())).thenReturn(Optional.of(cart));

        // When & Then
        CustomException exception = assertThrows(CustomException.class, () -> {
            cartService.updateItemQuantity(user.getId(), itemToUpdate.getId(), null);
        });

        assertThat(exception.getErrorCode()).isEqualTo(ApiResponseCode.INVALID_CART_ITEM_QUANTITY);
        assertThat(exception.getDetail()).isEqualTo("수량은 null일 수 없습니다.");
    }

    @Test
    @DisplayName("장바구니 상품 수량 변경 실패 - 수량이 0인 경우")
    void updateItemQuantity_Fail_QuantityValueInvalid_Zero() {
        // Given
        CartMenuItem itemToUpdate = CartFixture.cartMenuItem(cart, menuA, menuPriceA, List.of(option1), 7);
        ReflectionTestUtils.setField(itemToUpdate, "id", 7);
        ReflectionTestUtils.setField(cart, "cartMenuItems", new ArrayList<>(List.of(itemToUpdate)));

        when(cartRepository.findCartByUserId(user.getId())).thenReturn(Optional.of(cart));

        // When & Then
        CustomException exception = assertThrows(CustomException.class, () -> {
            cartService.updateItemQuantity(user.getId(), itemToUpdate.getId(), 0);
        });

        assertThat(exception.getErrorCode()).isEqualTo(ApiResponseCode.INVALID_CART_ITEM_QUANTITY);
        assertThat(exception.getDetail()).isEqualTo("수량은 1 이상이어야 합니다.");
    }

    @Test
    @DisplayName("장바구니 상품 수량 변경 실패 - 수량이 음수인 경우")
    void updateItemQuantity_Fail_QuantityValueInvalid_Negative () {
        // Given
        CartMenuItem itemToUpdate = CartFixture.cartMenuItem(cart, menuA, menuPriceA, List.of(option1), 7);
        ReflectionTestUtils.setField(itemToUpdate, "id", 7);
        ReflectionTestUtils.setField(cart, "cartMenuItems", new ArrayList<>(List.of(itemToUpdate)));

        when(cartRepository.findCartByUserId(user.getId())).thenReturn(Optional.of(cart));

        // When & Then
        CustomException exception = assertThrows(CustomException.class, () -> {
            cartService.updateItemQuantity(user.getId(), itemToUpdate.getId(), -999);
        });

        assertThat(exception.getErrorCode()).isEqualTo(ApiResponseCode.INVALID_CART_ITEM_QUANTITY);
        assertThat(exception.getDetail()).isEqualTo("수량은 1 이상이어야 합니다.");
    }


    @Test
    @DisplayName("장바구니 상품 삭제 성공 - 유효한 상품 ID인 경우")
    void deleteItem_Success_ValidCartItemId() {
        // Given
        CartMenuItem existingItem = CartFixture.cartMenuItem(cart, menuA, menuPriceA, List.of(option1), 1);
        ReflectionTestUtils.setField(existingItem, "id", 777);

        CartMenuItem deletedItem = CartFixture.cartMenuItem(cart, menuA, menuPriceB, List.of(option2), 3);
        ReflectionTestUtils.setField(deletedItem, "id", 888);

        ReflectionTestUtils.setField(cart, "cartMenuItems", new ArrayList<>(List.of(existingItem, deletedItem)));

        when(cartRepository.findCartByUserId(user.getId())).thenReturn(Optional.of(cart));

        // When
        cartService.deleteItem(user.getId(), deletedItem.getId());

        //Then
        assertThat(cart.getCartMenuItems()).hasSize(1);

        CartMenuItem existItem = cart.getCartMenuItems().get(0);

        assertThat(existItem.getId()).isEqualTo(existingItem.getId());
    }

    @Test
    @DisplayName("장바구니 상품 삭제 실패 - 유효하지 않은 상품 ID인 경우")
    void deleteItem_Fail_InvalidCartItemId() {
        // Given
        CartMenuItem existingItem = CartFixture.cartMenuItem(cart, menuA, menuPriceA, List.of(option1), 1);
        ReflectionTestUtils.setField(existingItem, "id", 777);

        CartMenuItem deletedItem = CartFixture.cartMenuItem(cart, menuA, menuPriceB, List.of(option2), 3);
        ReflectionTestUtils.setField(deletedItem, "id", 888);

        ReflectionTestUtils.setField(cart, "cartMenuItems", new ArrayList<>(List.of(existingItem, deletedItem)));

        when(cartRepository.findCartByUserId(user.getId())).thenReturn(Optional.of(cart));

        // When & Then
        CustomException exception = assertThrows(CustomException.class, () -> {
            cartService.deleteItem(user.getId(), 9999);
        });

        assertThat(exception.getErrorCode()).isEqualTo(ApiResponseCode.NOT_FOUND_CART_ITEM);
        assertThat(exception.getMessage()).isEqualTo("장바구니에 담긴 상품이 존재하지 않습니다");
    }
}
