package in.koreatech.koin.domain.order.order.model;

import java.util.Set;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OrderType {
    DELIVERY("배달", Set.of(
        OrderStatus.CONFIRMING,
        OrderStatus.COOKING,
        OrderStatus.DELIVERING,
        OrderStatus.DELIVERED,
        OrderStatus.CANCELED
    )),
    TAKE_OUT("포장", Set.of(
        OrderStatus.CONFIRMING,
        OrderStatus.COOKING,
        OrderStatus.PACKAGED,
        OrderStatus.PICKED_UP,
        OrderStatus.CANCELED
    )),
    ;

    private final String name;
    private final Set<OrderStatus> availableStatuses;

    public boolean supports(OrderStatus orderStatus) {
        return availableStatuses.contains(orderStatus);
    }
}
