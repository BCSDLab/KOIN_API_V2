package in.koreatech.koin.domain.order.order.dto.request;

import java.util.List;

import in.koreatech.koin.domain.order.order.model.OrderStatus;
import lombok.Getter;

@Getter
public enum OwnerOrderStatusCriteria {

    NEW(List.of(OrderStatus.CONFIRMING)),
    COOKING(List.of(OrderStatus.COOKING)),
    DELIVERING(List.of(OrderStatus.DELIVERING)),
    COMPLETED(List.of(OrderStatus.DELIVERED, OrderStatus.CANCELED)),
    ;

    private final List<OrderStatus> orderStatuses;

    OwnerOrderStatusCriteria(List<OrderStatus> orderStatuses) {
        this.orderStatuses = orderStatuses;
    }
}
