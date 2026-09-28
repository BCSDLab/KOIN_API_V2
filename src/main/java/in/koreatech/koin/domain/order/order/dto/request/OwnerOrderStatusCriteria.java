package in.koreatech.koin.domain.order.order.dto.request;

import java.util.List;

import in.koreatech.koin.domain.order.order.model.OrderStatus;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OwnerOrderStatusCriteria {

    NEW(List.of(OrderStatus.CONFIRMING)),
    COOKING(List.of(OrderStatus.COOKING)),
    DELIVERING(List.of(OrderStatus.DELIVERING, OrderStatus.PACKAGED)),
    COMPLETED(List.of(
        OrderStatus.DELIVERED,
        OrderStatus.PICKED_UP,
        OrderStatus.CANCELED
    )),
    ;

    private final List<OrderStatus> orderStatuses;
}
