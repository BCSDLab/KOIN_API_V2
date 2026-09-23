package in.koreatech.koin.domain.notification.service;

import static in.koreatech.koin.common.model.MobileAppPath.ORDER;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import in.koreatech.koin.common.event.OrderNotificationEvent;
import in.koreatech.koin.domain.notification.model.Notification;
import in.koreatech.koin.domain.notification.model.NotificationFactory;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderNotificationService {

    private final UserRepository userRepository;
    private final NotificationFactory notificationFactory;
    private final NotificationService notificationService;

    @Transactional
    public void pushNotification(OrderNotificationEvent event) {
        User user = userRepository.findById(event.userId()).orElse(null);
        if (user == null || !StringUtils.hasText(user.getDeviceToken())) {
            return;
        }

        Notification notification = notificationFactory.generateOrderNotification(
            ORDER,
            event.orderId(),
            event.shopName(),
            event.message(),
            event.estimatedTimeLabel(),
            event.estimatedAt(),
            user
        );
        notificationService.pushNotification(notification);
    }
}
