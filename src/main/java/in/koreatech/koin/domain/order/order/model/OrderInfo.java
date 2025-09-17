package in.koreatech.koin.domain.order.order.model;

import java.time.LocalDateTime;

public record OrderInfo(
    Integer orderId,
    Integer paymentId,
    Integer orderShopId,
    String orderableShopName,
    Boolean openStatus,
    String orderableShopThumbnail,
    LocalDateTime orderDate,
    OrderStatus orderStatus,
    String orderTitle,
    Integer totalAmount
) {
    
}
