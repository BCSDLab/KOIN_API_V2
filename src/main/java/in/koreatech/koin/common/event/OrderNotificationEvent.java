package in.koreatech.koin.common.event;

import java.time.LocalDateTime;

public record OrderNotificationEvent(
    Integer orderId,
    Integer userId,
    String shopName,
    String message,
    String estimatedTimeLabel,
    LocalDateTime estimatedAt
) {
}
