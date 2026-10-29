package in.koreatech.koin.common.event;

import java.time.LocalDateTime;

public record OrderNotificationEvent(
    Integer orderId,
    Integer userId,
    String shopName,
    String status,
    boolean delivery,
    LocalDateTime estimatedAt
) {

    public static OrderNotificationEvent of(
        Integer orderId,
        Integer userId,
        String shopName,
        String status,
        boolean delivery,
        LocalDateTime estimatedAt
    ) {
        return new OrderNotificationEvent(orderId, userId, shopName, status, delivery, estimatedAt);
    }
}
