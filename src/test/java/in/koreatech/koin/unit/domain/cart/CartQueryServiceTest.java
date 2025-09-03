package in.koreatech.koin.unit.domain.cart;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import in.koreatech.koin.domain.order.cart.dto.CartMenuItemEditResponse;
import in.koreatech.koin.domain.order.shop.model.entity.menu.*;
import in.koreatech.koin.global.code.ApiResponseCode;
import in.koreatech.koin.global.exception.CustomException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import in.koreatech.koin.domain.order.cart.dto.CartResponse;
import in.koreatech.koin.domain.order.cart.model.Cart;
import in.koreatech.koin.domain.order.cart.repository.CartRepository;
import in.koreatech.koin.domain.order.cart.service.CartQueryService;
import in.koreatech.koin.domain.order.model.OrderType;
import in.koreatech.koin.domain.order.shop.model.entity.shop.OrderableShop;
import in.koreatech.koin.domain.order.shop.repository.menu.OrderableShopMenuRepository;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.unit.fixture.CartFixture;
import in.koreatech.koin.unit.fixture.OrderableShopFixture;
import in.koreatech.koin.unit.fixture.OrderableShopMenuFixture;
import in.koreatech.koin.unit.fixture.UserFixture;

@ExtendWith(MockitoExtension.class)
public class CartQueryServiceTest {

    @InjectMocks
    private CartQueryService cartQueryService;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private OrderableShopMenuRepository orderableShopMenuRepository;

    private User user;
    private Cart cart;

    private OrderableShop orderableShop;
    private OrderableShopMenu menuGimbap;
    private OrderableShopMenu menuRamen;
    private OrderableShopMenuPrice gimbapPriceA;
    private OrderableShopMenuPrice gimbapPriceB;
    private OrderableShopMenuPrice ramenPriceA;
    private OrderableShopMenuOptionGroup toppingGroup;
    private OrderableShopMenuOptionGroup sourceGroup;
    private OrderableShopMenuOption toppingOptionA;
    private OrderableShopMenuOption toppingOptionB;
    private OrderableShopMenuOption sourceOptionA;
    private OrderableShopMenuOption sourceOptionB;

    private OrderableShopMenuOptionGroupMap toppingOptionGroupMap;
    private OrderableShopMenuOptionGroupMap sourceOptionGroupMap;

    @BeforeEach
    void setUp() {
        user = UserFixture.코인_유저();
        ReflectionTestUtils.setField(user, "id", 1);

        orderableShop = OrderableShopFixture.김밥천국(101);

        // 메뉴
        menuGimbap = OrderableShopMenuFixture.createMenu(orderableShop, "김밥", 201);
        menuRamen = OrderableShopMenuFixture.createMenu(orderableShop, "라면", 202);

        // 메뉴 세부 가격
        gimbapPriceA = OrderableShopMenuFixture.createMenuPrice(menuGimbap, "소고기 김밥", 6000, 301);
        gimbapPriceB = OrderableShopMenuFixture.createMenuPrice(menuGimbap, "참치 김밥", 5500, 302);
        ramenPriceA = OrderableShopMenuFixture.createMenuPrice(menuRamen, "신라면", 5500, 303);

        // 메뉴에 가격 설정
        ReflectionTestUtils.setField(menuRamen, "menuPrices", List.of(ramenPriceA));
        ReflectionTestUtils.setField(menuGimbap, "menuPrices", List.of(gimbapPriceA, gimbapPriceB));

        // 옵션 그룹
        toppingGroup = OrderableShopMenuFixture.createMenuOptionGroupWithEmptyMenuOption(
            orderableShop, "토핑 추가", 0, 2, false, 401);
        sourceGroup = OrderableShopMenuFixture.createMenuOptionGroupWithEmptyMenuOption(
            orderableShop, "소스 추가", 1, 2, true, 402);

        // 옵션
        toppingOptionA = OrderableShopMenuFixture.createMenuOption(toppingGroup, "치즈 추가", 500, 501);
        toppingOptionB = OrderableShopMenuFixture.createMenuOption(toppingGroup, "단무지 추가", 300, 502);
        sourceOptionA = OrderableShopMenuFixture.createMenuOption(sourceGroup, "캡사이신 추가", 300, 503);
        sourceOptionB = OrderableShopMenuFixture.createMenuOption(sourceGroup, "겨자 추가", 300, 504);

        // 옵션 그룹에 옵션들 설정
        ReflectionTestUtils.setField(toppingGroup, "menuOptions", List.of(toppingOptionA, toppingOptionB));
        ReflectionTestUtils.setField(sourceGroup, "menuOptions", List.of(sourceOptionA, sourceOptionB));

        // 옵션 그룹 맵 생성
        toppingOptionGroupMap = OrderableShopMenuFixture.createMenuOptionGroupMap(toppingGroup, menuGimbap, 601);
        sourceOptionGroupMap = OrderableShopMenuFixture.createMenuOptionGroupMap(sourceGroup, menuRamen, 602);

        // 메뉴에 옵션 그룹 맵 설정
        ReflectionTestUtils.setField(menuGimbap, "menuOptionGroupMap", List.of(toppingOptionGroupMap));
        ReflectionTestUtils.setField(menuRamen, "menuOptionGroupMap", List.of(sourceOptionGroupMap));

        // 메뉴 이미지 설정
        ReflectionTestUtils.setField(menuGimbap, "menuImages", List.of());
        ReflectionTestUtils.setField(menuRamen, "menuImages", List.of());

        // 장바구니 생성
        cart = CartFixture.createCart(user, orderableShop);
    }

    @Nested
    @DisplayName("장바구니 아이템 조회 테스트")
    class GetCartItemsTest {

        @Test
        void 유저의_장바구니가_비어있지않으면_장바구니의_아이템이_조회된다() {
            // given
            Integer userId = user.getId();
            OrderType orderType = OrderType.DELIVERY;
            cart.addItem(menuGimbap, gimbapPriceA, List.of(toppingOptionA, toppingOptionB), 1);
            cart.addItem(menuRamen, ramenPriceA, List.of(sourceOptionA), 2);
            when(cartRepository.findCartByUserId(userId)).thenReturn(Optional.of(cart));

            // when
            CartResponse result = cartQueryService.getCartItems(userId, orderType);

            // then
            assertThat(result).isNotNull();
            assertThat(result.items()).hasSize(2);
            assertThat(result.items().get(0).name()).isEqualTo("김밥");
            assertThat(result.items().get(0).totalAmount()).isEqualTo(6800);
            assertThat(result.items().get(1).name()).isEqualTo("라면");
            assertThat(result.items().get(1).totalAmount()).isEqualTo(11600);
        }

        @Test
        void 유저의_장바구니가_비어있으면_빈_장바구니_응답이_반환된다() {
            // given
            Integer userId = user.getId();
            OrderType orderType = OrderType.DELIVERY;
            when(cartRepository.findCartByUserId(userId)).thenReturn(Optional.of(cart));

            // when
            CartResponse result = cartQueryService.getCartItems(userId, orderType);

            // then
            assertThat(result).isNotNull();
            assertThat(result.items()).isEmpty();
            assertThat(result.totalAmount()).isEqualTo(0);
        }

        @Test
        void 유저의_장바구니가_존재하지않으면_빈_장바구니_응답이_반환된다() {
            // given
            Integer userId = user.getId();
            OrderType orderType = OrderType.DELIVERY;
            when(cartRepository.findCartByUserId(userId)).thenReturn(Optional.empty());

            // when
            CartResponse result = cartQueryService.getCartItems(userId, orderType);

            // then
            assertThat(result).isNotNull();
            assertThat(result.items()).isEmpty();
            assertThat(result.totalAmount()).isEqualTo(0);
        }

        @Test
        void 배달이_불가능한_가게의_배달주문_장바구니_조회시_예외가_발생한다() {
            // given
            Integer userId = user.getId();
            OrderType orderType = OrderType.DELIVERY;
            ReflectionTestUtils.setField(orderableShop, "delivery", false); // 배달 불가능 설정
            cart.addItem(menuGimbap, gimbapPriceA, List.of(toppingOptionA, toppingOptionB), 1);
            cart.addItem(menuRamen, ramenPriceA, List.of(sourceOptionA), 2);
            when(cartRepository.findCartByUserId(userId)).thenReturn(Optional.of(cart));

            // when & then
            assertEquals(ApiResponseCode.SHOP_NOT_DELIVERABLE,
                assertThrows(CustomException.class,
                    () -> cartQueryService.getCartItems(userId, orderType)).getErrorCode());
        }

        @Test
        void 포장이_불가능한_가게의_포장주문_장바구니_조회시_예외가_발생한다() {
            // given
            Integer userId = user.getId();
            OrderType orderType = OrderType.TAKE_OUT;
            ReflectionTestUtils.setField(orderableShop, "takeout", false); // 포장 불가능 설정
            cart.addItem(menuGimbap, gimbapPriceA, List.of(toppingOptionA, toppingOptionB), 1);
            cart.addItem(menuRamen, ramenPriceA, List.of(sourceOptionA), 2);
            when(cartRepository.findCartByUserId(userId)).thenReturn(Optional.of(cart));

            // when & then
            assertEquals(ApiResponseCode.SHOP_NOT_TAKEOUT_AVAILABLE,
                assertThrows(CustomException.class,
                    () -> cartQueryService.getCartItems(userId, orderType)).getErrorCode());
        }

    }

    @Nested
    @DisplayName("장바구니 아이템 옵션 수정용 조회 테스트")
    class getOrderableShopMenuForEditOptionsTest {

        private Integer userId;
        private Integer gimbapCartItemId;

        @BeforeEach
        void setUpCartItems() {
            userId = user.getId();
            cart.addItem(menuGimbap, gimbapPriceA, List.of(toppingOptionA, toppingOptionB), 1);
            cart.addItem(menuRamen, ramenPriceA, List.of(sourceOptionA), 2);
            ReflectionTestUtils.setField(cart.getCartMenuItems().get(0), "id", 1001);
            ReflectionTestUtils.setField(cart.getCartMenuItems().get(1), "id", 1002);
            gimbapCartItemId = cart.getCartMenuItems().get(0).getId();
        }

        @Test
        void 조회하는_장바구니아이템이_장바구니에_존재하면_해당_장바구니아이템의_메뉴와_옵션들이_조회된다() {
            // given
            when(cartRepository.findCartByUserId(userId)).thenReturn(Optional.of(cart));
            when(orderableShopMenuRepository.getByIdWithMenuOptionGroups(menuGimbap.getId())).thenReturn(menuGimbap);

            // when
            CartMenuItemEditResponse response = cartQueryService.getOrderableShopMenuForEditOptions(userId,
                gimbapCartItemId);

            // then
            assertThat(response).isNotNull();
            assertThat(response.id()).isEqualTo(menuGimbap.getId());
            assertThat(response.name()).isEqualTo(menuGimbap.getName());
            assertThat(response.description()).isEqualTo(menuGimbap.getDescription());
            assertThat(response.prices()).hasSize(2);
            assertThat(response.optionGroups()).hasSize(1);
            assertThat(response.optionGroups().get(0).name()).isEqualTo(toppingGroup.getName());
        }

        @Test
        void 조회하는_장바구니아이템이_장바구니에_없으면_예외가_발생한다() {
            // given
            Integer strangeCartMenuItemId = 999;
            when(cartRepository.findCartByUserId(userId)).thenReturn(Optional.of(cart));

            // when & then
            assertEquals(ApiResponseCode.NOT_FOUND_CART_ITEM,
                assertThrows(CustomException.class, () -> cartQueryService.getOrderableShopMenuForEditOptions(userId,
                    strangeCartMenuItemId)).getErrorCode());
        }

        @Test
        void 유저의_장바구니가_존재하지않으면_예외가_발생한다() {
            // given
            when(cartRepository.findCartByUserId(userId)).thenReturn(Optional.empty());

            // when & then
            assertEquals(ApiResponseCode.NOT_FOUND_CART,
                assertThrows(CustomException.class, () ->
                    cartQueryService.getOrderableShopMenuForEditOptions(userId, gimbapCartItemId)).getErrorCode());
        }

        @Test
        void 존재하지않는_메뉴의_장바구니아이템을_조회하면_예외가_발생한다() {
            // given
            OrderableShopMenu nonExistentMenu = OrderableShopMenuFixture.createMenu(orderableShop, "존재하지않는메뉴", 999);
            OrderableShopMenuPrice nonExistentMenuPrice = OrderableShopMenuFixture.createMenuPrice(nonExistentMenu,
                "존재하지않는메뉴종류", 1000, 999);
            ReflectionTestUtils.setField(nonExistentMenu, "menuPrices", List.of(nonExistentMenuPrice));
            ReflectionTestUtils.setField(nonExistentMenu, "menuImages", List.of());

            cart.getCartMenuItems().clear();
            cart.addItem(nonExistentMenu, nonExistentMenuPrice, List.of(), 1);
            ReflectionTestUtils.setField(cart.getCartMenuItems().get(0), "id", 1003);
            Integer nonExistentCartItemId = cart.getCartMenuItems().get(0).getId();

            when(cartRepository.findCartByUserId(userId)).thenReturn(Optional.of(cart));
            when(orderableShopMenuRepository.getByIdWithMenuOptionGroups(nonExistentMenu.getId())).thenThrow(
                CustomException.of(ApiResponseCode.NOT_FOUND_ORDERABLE_SHOP_MENU));

            // when & then
            assertEquals(ApiResponseCode.NOT_FOUND_ORDERABLE_SHOP_MENU,
                assertThrows(CustomException.class, () ->
                    cartQueryService.getOrderableShopMenuForEditOptions(userId, nonExistentCartItemId)).getErrorCode());
        }
    }
}

