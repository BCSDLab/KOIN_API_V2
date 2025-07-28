package in.koreatech.koin.unit.domain.cart;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.spy;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import in.koreatech.koin.domain.order.cart.model.Cart;
import in.koreatech.koin.domain.order.cart.model.CartMenuItem;
import in.koreatech.koin.domain.order.shop.model.entity.menu.OrderableShopMenu;
import in.koreatech.koin.domain.order.shop.model.entity.menu.OrderableShopMenuOption;
import in.koreatech.koin.domain.order.shop.model.entity.menu.OrderableShopMenuPrice;
import in.koreatech.koin.domain.order.shop.model.entity.shop.OrderableShop;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.unit.fixutre.CartFixture;
import in.koreatech.koin.unit.fixutre.OrderableShopFixture;
import in.koreatech.koin.unit.fixutre.OrderableShopMenuFixture;
import in.koreatech.koin.unit.fixutre.UserFixture;

@ExtendWith(MockitoExtension.class)
public class CartTest {

    private User user;
    private OrderableShop orderableShop;
    private OrderableShopMenu menuGimbap;
    private OrderableShopMenu menuRamen;
    private OrderableShopMenuPrice menuGimbapPrice;
    private OrderableShopMenuPrice menuRamenPrice;
    private List<OrderableShopMenuOption> menuGimbapOptions;
    private List<OrderableShopMenuOption> menuRamenOptions;

    @BeforeEach
    void setUp() {
        user = UserFixture.코인_유저();
        orderableShop = OrderableShopFixture.김밥천국(101);

        menuGimbap = OrderableShopMenuFixture.createMenu(orderableShop, "김밥", 15);
        menuGimbapPrice = OrderableShopMenuFixture.createMenuPrice(menuGimbap, "소고기 김밥", 6000, 30);
        menuGimbapOptions = List.of(
            OrderableShopMenuFixture.createMenuOption("단무지", 1000, 45)
        );

        menuRamen = OrderableShopMenuFixture.createMenu(orderableShop, "라면", 16);
        menuRamenPrice = OrderableShopMenuFixture.createMenuPrice(menuRamen, "신라면", 5500, 303);
        menuRamenOptions = List.of(
            OrderableShopMenuFixture.createMenuOption("단무지", 1000, 45)
        );

        ReflectionTestUtils.setField(menuGimbap, "menuPrices", List.of(menuGimbapPrice));
        ReflectionTestUtils.setField(menuRamen, "menuPrices", List.of(menuRamenPrice));
    }

    @Nested
    class AddItem {

        @Test
        void 장바구니가_비어있을때_새로운_상품을_추가하면_상품이_담긴다() {
            // given
            Cart cart = spy(CartFixture.createCart(user, orderableShop));

            // when
            cart.addItem(menuGimbap, menuGimbapPrice, menuGimbapOptions, 1);

            // then
            List<CartMenuItem> cartMenuItems = cart.getCartMenuItems();
            assertEquals(1, cartMenuItems.size());

            CartMenuItem addedItem = cartMenuItems.get(0);
            assertEquals(1, addedItem.getQuantity());
            assertEquals(menuGimbap.getId(), addedItem.getOrderableShopMenu().getId());
            assertEquals(menuGimbapPrice.getId(), addedItem.getOrderableShopMenuPrice().getId());
            assertThat(addedItem.getCartMenuItemOptions())
                .extracting(option -> option.getOrderableShopMenuOption().getId())
                .containsExactlyInAnyOrder(45);
        }

        @Test
        void 장바구니에_동일한_상품이_존재하면_수량이_1_증가한다() {
            // given
            Cart cart = spy(CartFixture.createCart(user, orderableShop));
            cart.addItem(menuGimbap, menuGimbapPrice, menuGimbapOptions, 1);

            // when
            cart.addItem(menuGimbap, menuGimbapPrice, menuGimbapOptions, 1);

            // then
            List<CartMenuItem> cartMenuItems = cart.getCartMenuItems();
            assertThat(cartMenuItems).hasSize(1);

            CartMenuItem existingItem = cartMenuItems.get(0);
            assertEquals(2, existingItem.getQuantity());
        }

        @Test
        void 기존과_다른_상품을_추가하면_새로운_상품으로_담긴다() {
            // given
            Cart cart = spy(CartFixture.createCart(user, orderableShop));
            cart.addItem(menuGimbap, menuGimbapPrice, menuGimbapOptions, 1);

            OrderableShopMenu newMenu = OrderableShopMenuFixture.createMenu(orderableShop, "라면", 16);
            OrderableShopMenuPrice newMenuPrice = OrderableShopMenuFixture.createMenuPrice(newMenu, "매운맛", 7000, 31);

            // when
            cart.addItem(newMenu, newMenuPrice, Collections.emptyList(), 1);

            // then
            List<CartMenuItem> cartMenuItems = cart.getCartMenuItems();
            assertThat(cartMenuItems).hasSize(2);

            CartMenuItem itemKimBap = cartMenuItems.stream()
                .filter(item -> item.getOrderableShopMenu().getName().equals("김밥"))
                .findFirst()
                .orElseThrow();
            assertEquals(1, itemKimBap.getQuantity());
            assertEquals("김밥", itemKimBap.getOrderableShopMenu().getName());

            CartMenuItem itemRamen = cartMenuItems.stream()
                .filter(item -> item.getOrderableShopMenu().getName().equals("라면"))
                .findFirst()
                .orElseThrow();
            assertEquals(1, itemRamen.getQuantity());
            assertEquals("라면", itemRamen.getOrderableShopMenu().getName());
        }
    }

    @Test
    void 장바구니에_담긴_상품의_종류와_총_수량을_확인한다() {
        // Given
        Cart cart = spy(CartFixture.createCart(user, orderableShop));
        cart.addItem(menuGimbap, menuGimbapPrice, menuGimbapOptions, 3);
        cart.addItem(menuRamen, menuRamenPrice, menuRamenOptions, 4);

        // When
        Integer itemTypeCount = cart.getItemTypeCount();
        Integer totalQuantity = cart.getTotalQuantity();

        // Then
        assertEquals(2, itemTypeCount);
        assertEquals(7, totalQuantity);
    }
}
