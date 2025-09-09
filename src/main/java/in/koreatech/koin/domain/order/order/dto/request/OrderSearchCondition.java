package in.koreatech.koin.domain.order.order.dto.request;

public record OrderSearchCondition(
    Integer page,
    Integer limit,
    OrderSearchPeriodCriteria period,
    OrderStatusCriteria status,
    OrderTypeCriteria type
) {
    public static OrderSearchCondition of(
        Integer page,
        Integer limit,
        OrderSearchPeriodCriteria period,
        OrderStatusCriteria status,
        OrderTypeCriteria type
    ) {
        return new OrderSearchCondition(page, limit, period, status, type);
    }
}
