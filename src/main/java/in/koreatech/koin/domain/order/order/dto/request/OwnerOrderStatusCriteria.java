package in.koreatech.koin.domain.order.order.dto.request;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import in.koreatech.koin.domain.order.order.model.OrderStatus;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OwnerOrderStatusCriteria {

    NEW(List.of(OrderStatus.CONFIRMING)),
    COOKING(List.of(OrderStatus.COOKING)),
    DELIVERING(List.of(OrderStatus.DELIVERING)),
    COMPLETED(List.of(OrderStatus.DELIVERED, OrderStatus.CANCELED)),
    ;

    private final List<OrderStatus> orderStatuses;

    public static List<OrderStatus> allOrderStatuses() {
        return Arrays.stream(values())
            .flatMap(criteria -> criteria.orderStatuses.stream())
            .distinct()
            .toList();
    }

    public long sumCount(Map<OrderStatus, Long> countByStatus) {
        return orderStatuses.stream()
            .mapToLong(orderStatus -> countByStatus.getOrDefault(orderStatus, 0L))
            .sum();
    }
}
