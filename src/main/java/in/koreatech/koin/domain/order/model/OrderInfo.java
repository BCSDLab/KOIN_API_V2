package in.koreatech.koin.domain.order.model;

import java.time.LocalDate;

public record OrderInfo(
    Integer orderId,
    Integer paymentId,
    Integer orderShopId,
    String orderableShopName,
    LocalDate orderDate,
    String orderStatus,
    String orderTitle
) {
    
}
