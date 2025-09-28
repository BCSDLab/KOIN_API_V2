package in.koreatech.koin.domain.order.order.dto.request;

public record OrderSearchCondition(
    Integer page,
    Integer limit,
    OrderSearchPeriodCriteria period,
    OrderStatusCriteria status,
    OrderTypeCriteria type,
    String query
) {
    public static OrderSearchCondition of(
        Integer page,
        Integer limit,
        OrderSearchPeriodCriteria period,
        OrderStatusCriteria status,
        OrderTypeCriteria type,
        String query
    ) {
        return new OrderSearchCondition(page, limit, period, status, type, query);
    }
}
