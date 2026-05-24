package in.koreatech.koin.domain.notification.service;

import static in.koreatech.koin.common.model.MobileAppPath.SHOP;
import static in.koreatech.koin.domain.notification.model.NotificationSubscribeType.SHOP_EVENT;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.notification.model.Notification;
import in.koreatech.koin.domain.notification.model.NotificationFactory;
import in.koreatech.koin.domain.notification.repository.NotificationSubscribeRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ShopNotificationService {

    private final NotificationService notificationService;
    private final NotificationFactory notificationFactory;
    private final NotificationSubscribeRepository notificationSubscribeRepository;

    public void sendShopEventCreateNotifications(Integer shopId, String shopName, String title, String thumbnailImage) {
        List<Notification> notifications = notificationSubscribeRepository
            .findAllBySubscribeTypeAndDetailTypeIsNull(SHOP_EVENT)
            .stream()
            .filter(subscribe -> subscribe.getUser().getDeviceToken() != null)
            .map(subscribe -> notificationFactory.generateShopEventCreateNotification(
                SHOP,
                shopId,
                thumbnailImage,
                shopName,
                title,
                subscribe.getUser()
            )).toList();
        notificationService.pushNotifications(notifications);
    }
}
