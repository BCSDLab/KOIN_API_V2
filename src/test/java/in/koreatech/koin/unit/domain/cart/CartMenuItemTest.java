package in.koreatech.koin.unit.domain.cart;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import in.koreatech.koin.domain.order.cart.model.Cart;
import in.koreatech.koin.domain.order.cart.model.CartMenuItem;
import in.koreatech.koin.domain.order.shop.model.entity.menu.OrderableShopMenu;
import in.koreatech.koin.domain.order.shop.model.entity.menu.OrderableShopMenuOption;
import in.koreatech.koin.domain.order.shop.model.entity.menu.OrderableShopMenuPrice;
import in.koreatech.koin.domain.order.shop.model.entity.shop.OrderableShop;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.global.exception.CustomException;
import in.koreatech.koin.unit.fixture.CartFixture;
import in.koreatech.koin.unit.fixture.OrderableShopFixture;
import in.koreatech.koin.unit.fixture.OrderableShopMenuFixture;
import in.koreatech.koin.unit.fixture.UserFixture;

@ExtendWith(MockitoExtension.class)
class CartMenuItemTest {

    private User user;
    private OrderableShop orderableShop;
    private Cart cart;
    private OrderableShopMenu menu;
    private OrderableShopMenuPrice menuPrice;
    private List<OrderableShopMenuOption> menuOptions;

    @BeforeEach
    void setUp() {
        user = UserFixture.코인_유저();
        orderableShop = OrderableShopFixture.김밥천국(101);
        cart = CartFixture.createCart(user, orderableShop);

        menu = OrderableShopMenuFixture.createMenu(orderableShop, "김밥", 1);
        menuPrice = OrderableShopMenuFixture.createMenuPrice(menu, "소고기 김밥", 6000, 1);
        menuOptions = List.of(
                OrderableShopMenuFixture.createMenuOption("단무지", 1000, 1),
                OrderableShopMenuFixture.createMenuOption("치즈", 500, 2)
        );
    }

    private CartMenuItem createCartMenuItem(int quantity) {
        return CartMenuItem.create(cart, menu, menuPrice, menuOptions, quantity);
    }

    @Nested
    class CreateTest {

        @Test
        void 정상적인_값으로_CartMenuItem을_생성하면_성공한다() {
            // given
            int quantity = 2;

            // when
            CartMenuItem cartMenuItem = createCartMenuItem(quantity);

            // then
            assertThat(cartMenuItem.getCart()).isEqualTo(cart);
            assertThat(cartMenuItem.getOrderableShopMenu()).isEqualTo(menu);
            assertThat(cartMenuItem.getOrderableShopMenuPrice()).isEqualTo(menuPrice);
            assertThat(cartMenuItem.getQuantity()).isEqualTo(2);
            assertThat(cartMenuItem.getIsModified()).isFalse();
            assertThat(cartMenuItem.getCartMenuItemOptions()).hasSize(2);
        }

        @Test
        void 수량이_null이거나_0이하면_예외가_발생한다() {
            // given
            Integer nullQuantity = null;
            Integer zeroQuantity = 0;

            // when & then
            assertThatThrownBy(() -> CartMenuItem.create(cart, menu, menuPrice, menuOptions, nullQuantity))
                    .isInstanceOf(CustomException.class);

            assertThatThrownBy(() -> CartMenuItem.create(cart, menu, menuPrice, menuOptions, zeroQuantity))
                    .isInstanceOf(CustomException.class);
        }
    }

    @Nested
    class UpdateQuantityTest {

        @Test
        void 정상적인_수량으로_업데이트하면_수량이_변경된다() {
            // given
            CartMenuItem cartMenuItem = createCartMenuItem(1);
            int newQuantity = 5;

            // when
            cartMenuItem.updateQuantity(newQuantity);

            // then
            assertThat(cartMenuItem.getQuantity()).isEqualTo(5);
        }

        @Test
        void 잘못된_수량으로_업데이트하면_예외가_발생한다() {
            // given
            CartMenuItem cartMenuItem = createCartMenuItem(1);
            Integer nullQuantity = null;
            Integer zeroQuantity = 0;

            // when & then
            assertThatThrownBy(() -> cartMenuItem.updateQuantity(nullQuantity))
                    .isInstanceOf(CustomException.class);

            assertThatThrownBy(() -> cartMenuItem.updateQuantity(zeroQuantity))
                    .isInstanceOf(CustomException.class);
        }
    }

    @Nested
    class IncreaseQuantityTest {

        @Test
        void 수량을_증가시키면_수량이_증가한다() {
            // given
            CartMenuItem cartMenuItem = createCartMenuItem(2);
            int expectedQuantity = 6; // 2 + 1 + 3

            // when
            cartMenuItem.increaseQuantity();
            cartMenuItem.increaseQuantity(3);

            // then
            assertThat(cartMenuItem.getQuantity()).isEqualTo(expectedQuantity);
        }
    }

    @Nested
    class IsSameItemTest {

        @Test
        void 동일한_메뉴_가격_옵션이면_true를_반환한다() {
            // given
            CartMenuItem cartMenuItem = createCartMenuItem(1);

            // when
            boolean result = cartMenuItem.isSameItem(menu, menuPrice, menuOptions);

            // then
            assertThat(result).isTrue();
        }

        @Test
        void 다른_메뉴_가격_옵션이면_false를_반환한다() {
            // given
            CartMenuItem cartMenuItem = createCartMenuItem(1);
            OrderableShopMenu otherMenu = OrderableShopMenuFixture.createMenu(orderableShop, "라면", 2);
            OrderableShopMenuPrice otherMenuPrice = OrderableShopMenuFixture.createMenuPrice(otherMenu, "신라면", 5500, 2);
            List<OrderableShopMenuOption> otherOptions = List.of(
                    OrderableShopMenuFixture.createMenuOption("계란", 1000, 3)
            );

            // when & then
            assertThat(cartMenuItem.isSameItem(otherMenu, menuPrice, menuOptions)).isFalse();
            assertThat(cartMenuItem.isSameItem(menu, otherMenuPrice, menuOptions)).isFalse();
            assertThat(cartMenuItem.isSameItem(menu, menuPrice, otherOptions)).isFalse();
        }

        @Test
        void 옵션_순서가_달라도_구성이_같으면_true를_반환한다() {
            // given
            CartMenuItem cartMenuItem = createCartMenuItem(1);
            List<OrderableShopMenuOption> reverseOptions = List.of(menuOptions.get(1), menuOptions.get(0));

            // when
            boolean result = cartMenuItem.isSameItem(menu, menuPrice, reverseOptions);

            // then
            assertThat(result).isTrue();
        }
    }

    @Nested
    class UpdateTest {

        @Test
        void 새로운_가격과_옵션으로_업데이트하면_정보가_변경되고_수정_상태가_된다() {
            // given
            CartMenuItem cartMenuItem = createCartMenuItem(1);
            OrderableShopMenuPrice newPrice = OrderableShopMenuFixture.createMenuPrice(menu, "특가 소고기 김밥", 5500, 3);
            List<OrderableShopMenuOption> newOptions = List.of(
                    OrderableShopMenuFixture.createMenuOption("김치", 500, 4)
            );
            int newQuantity = 3;

            // when
            cartMenuItem.update(newPrice, newOptions, newQuantity);

            // then
            assertThat(cartMenuItem.getOrderableShopMenuPrice()).isEqualTo(newPrice);
            assertThat(cartMenuItem.getQuantity()).isEqualTo(3);
            assertThat(cartMenuItem.getCartMenuItemOptions()).hasSize(1);
            assertThat(cartMenuItem.getIsModified()).isTrue();
        }

        @Test
        void 잘못된_수량으로_업데이트하면_예외가_발생한다() {
            // given
            CartMenuItem cartMenuItem = createCartMenuItem(1);
            OrderableShopMenuPrice newPrice = OrderableShopMenuFixture.createMenuPrice(menu, "특가 소고기 김밥", 5500, 3);
            int invalidQuantity = 0;

            // when & then
            assertThatThrownBy(() -> cartMenuItem.update(newPrice, List.of(), invalidQuantity))
                    .isInstanceOf(CustomException.class);
        }
    }

    @Nested
    class CalculateTotalAmountTest {

        @Test
        void 메뉴_가격과_옵션_가격을_수량만큼_곱한_총_금액을_반환한다() {
            // given
            CartMenuItem cartMenuItem = createCartMenuItem(2);
            int expectedAmount = 15000; // (6000 + 1500) * 2

            // when
            Integer totalAmount = cartMenuItem.calculateTotalAmount();

            // then
            assertThat(totalAmount).isEqualTo(expectedAmount);
        }

        @Test
        void 옵션이_없는_아이템의_총_금액을_정확히_계산한다() {
            // given
            CartMenuItem noOptionItem = CartMenuItem.create(cart, menu, menuPrice, List.of(), 3);
            int expectedAmount = 18000; // 6000 * 3

            // when
            Integer totalAmount = noOptionItem.calculateTotalAmount();

            // then
            assertThat(totalAmount).isEqualTo(expectedAmount);
        }
    }
}
