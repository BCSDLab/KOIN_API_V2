package in.koreatech.koin.domain.order.model;

import java.time.LocalDateTime;

public record OrderInfo(
    Integer orderId,
    Integer paymentId,
    Integer orderShopId,
    String orderableShopName,
    LocalDateTime orderDate,
    OrderStatus orderStatus,
    String orderTitle
) {
    
}
