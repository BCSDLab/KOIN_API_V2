package in.koreatech.koin.domain.payment.model.domain;

import in.koreatech.koin.domain.order.order.model.OrderMenu;
import in.koreatech.koin.domain.order.order.model.OrderMenuOption;

public record TemporaryMenuOption(
    String optionGroupName,
    String optionName,
    Integer quantity,
    Integer optionPrice
) {
    public OrderMenuOption toOrderMenuOption(OrderMenu orderMenu) {
        return OrderMenuOption.builder()
            .optionGroupName(optionGroupName)
            .optionName(optionName)
            .optionPrice(optionPrice)
            .quantity(quantity)
            .orderMenu(orderMenu)
            .build();
    }
}
