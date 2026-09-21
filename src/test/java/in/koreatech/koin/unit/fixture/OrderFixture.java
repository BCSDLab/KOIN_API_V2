package in.koreatech.koin.unit.fixture;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.test.util.ReflectionTestUtils;

import in.koreatech.koin.domain.order.order.model.Order;
import in.koreatech.koin.domain.order.order.model.OrderDelivery;
import in.koreatech.koin.domain.order.order.model.OrderMenu;
import in.koreatech.koin.domain.order.order.model.OrderMenuOption;
import in.koreatech.koin.domain.order.order.model.OrderStatus;
import in.koreatech.koin.domain.order.order.model.OrderType;
import in.koreatech.koin.domain.order.shop.model.entity.shop.OrderableShop;
import in.koreatech.koin.domain.user.model.User;

public class OrderFixture {

    private OrderFixture() {}

    public static Order 배달_주문(
        Integer id,
        String orderNumber,
        OrderStatus status,
        OrderableShop orderableShop,
        User user
    ) {
        Order order = Order.builder()
            .pgOrderId("pg-order-" + id)
            .orderNumber(orderNumber)
            .orderType(OrderType.DELIVERY)
            .status(status)
            .orderableShopName(orderableShop.getShop().getName())
            .orderableShopAddress(orderableShop.getShop().getAddress())
            .orderableShopAddressDetail("1층")
            .phoneNumber("01012341234")
            .totalProductPrice(32000)
            .discountAmount(0)
            .totalPrice(35000)
            .orderableShop(orderableShop)
            .user(user)
            .isDeleted(false)
            .orderMenus(new ArrayList<>())
            .build();

        ReflectionTestUtils.setField(order, "id", id);
        ReflectionTestUtils.setField(order, "createdAt", LocalDateTime.of(2026, 9, 20, 18, 42));

        order.setOrderDelivery(OrderDelivery.builder()
            .order(order)
            .address("충청남도 천안시 동남구 병천면 충절로 1600")
            .addressDetail("2공학관 201호")
            .latitude(BigDecimal.valueOf(36.7645))
            .longitude(BigDecimal.valueOf(127.2818))
            .toOwner("문 앞에 두고 벨 눌러주세요.")
            .toRider("빠르게 부탁드려요.")
            .deliveryTip(3000)
            .provideCutlery(true)
            .build());

        return order;
    }

    public static OrderMenu 짜장면(Order order) {
        OrderMenu orderMenu = OrderMenu.builder()
            .order(order)
            .menuName("짜장면")
            .menuPrice(14000)
            .menuPriceName("곱빼기")
            .quantity(2)
            .isDeleted(false)
            .orderMenuOptions(new ArrayList<>())
            .build();

        ReflectionTestUtils.setField(orderMenu, "id", 1);

        OrderMenuOption orderMenuOption = OrderMenuOption.builder()
            .orderMenu(orderMenu)
            .optionGroupName("추가 선택")
            .optionName("단무지 추가")
            .optionPrice(500)
            .quantity(1)
            .isDeleted(false)
            .build();

        ReflectionTestUtils.setField(orderMenu, "orderMenuOptions", List.of(orderMenuOption));

        return orderMenu;
    }
}
