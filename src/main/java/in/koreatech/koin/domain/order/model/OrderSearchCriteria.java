package in.koreatech.koin.domain.order.model;

import in.koreatech.koin.domain.order.order.dto.request.OrderSearchCondition;
import in.koreatech.koin.domain.order.order.dto.request.OrderSearchPeriodCriteria;
import in.koreatech.koin.domain.order.order.dto.request.OrderStatusCriteria;
import in.koreatech.koin.domain.order.order.dto.request.OrderTypeCriteria;

public record OrderSearchCriteria(
    Integer page,
    Integer limit,
    OrderSearchPeriodCriteria period,
    OrderStatusCriteria status,
    OrderTypeCriteria type
) {
    public static OrderSearchCriteria from(OrderSearchCondition condition) {
        return new OrderSearchCriteria(
            condition.page(),
            condition.limit(),
            condition.period(),
            condition.status(),
            condition.type()
        );
    }
}
