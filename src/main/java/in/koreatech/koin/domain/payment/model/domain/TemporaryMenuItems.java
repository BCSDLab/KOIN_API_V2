package in.koreatech.koin.domain.payment.model.domain;

import java.util.List;

import in.koreatech.koin.domain.order.model.Order;
import in.koreatech.koin.domain.order.model.OrderMenu;
import in.koreatech.koin.domain.order.model.OrderMenuOption;

public record TemporaryMenuItems(
    String name,
    Integer quantity,
    Integer totalAmount,
    TemporaryMenuPrice price,
    List<TemporaryMenuOption> options
) {
    public OrderMenu toOrderMenu(Order order) {
        OrderMenu orderMenu = OrderMenu.builder()
            .menuName(name)
            .quantity(quantity)
            .menuPrice(price.price())
            .menuPriceName(price.name())
            .order(order)
            .build();

        if (options != null) {
            List<OrderMenuOption> orderMenuOptions = options.stream()
                .map(temporaryMenuOption -> temporaryMenuOption.toOrderMenuOption(orderMenu))
                .toList();
            orderMenu.setOrderMenuOptions(orderMenuOptions);
        }

        return orderMenu;
    }
}
