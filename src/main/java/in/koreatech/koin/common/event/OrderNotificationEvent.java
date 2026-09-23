package in.koreatech.koin.common.event;

import java.time.LocalDateTime;

public record OrderNotificationEvent(
    Integer orderId,
    Integer userId,
    String shopName,
    OrderNotificationType type,
    LocalDateTime estimatedAt
) {

    public static OrderNotificationEvent of(
        Integer orderId,
        Integer userId,
        String shopName,
        OrderNotificationType type,
        LocalDateTime estimatedAt
    ) {
        return new OrderNotificationEvent(orderId, userId, shopName, type, estimatedAt);
    }
}
