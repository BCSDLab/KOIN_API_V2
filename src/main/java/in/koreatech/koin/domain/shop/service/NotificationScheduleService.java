package in.koreatech.koin.domain.shop.service;

import static in.koreatech.koin.global.fcm.MobileAppPath.SHOP;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.shop.exception.NotificationMessageNotFoundException;
import in.koreatech.koin.domain.shop.model.shop.Shop;
import in.koreatech.koin.domain.shop.model.shop.ShopCategoryMap;
import in.koreatech.koin.domain.shop.model.shop.ShopNotificationMessage;
import in.koreatech.koin.domain.shop.model.shop.ShopNotificationQueue;
import in.koreatech.koin.domain.shop.repository.shop.ShopNotificationQueueRepository;
import in.koreatech.koin.domain.user.model.User;

import in.koreatech.koin.global.domain.notification.model.Notification;
import in.koreatech.koin.global.domain.notification.model.NotificationFactory;
import in.koreatech.koin.global.domain.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationScheduleService {

    private final ShopNotificationQueueRepository shopNotificationQueueRepository;
    private final NotificationFactory notificationFactory;
    private final NotificationService notificationService;

    @Transactional
    public void sendDueNotifications() {
        LocalDateTime now = LocalDateTime.now();

        List<ShopNotificationQueue> dueNotifications = shopNotificationQueueRepository.findByNotificationTimeBefore(now);

        if (dueNotifications.isEmpty()) {
            return;
        }

        List<Notification> notifications = dueNotifications.stream()
            .map(this::createNotification)
            .toList();

        shopNotificationQueueRepository.deleteByNotificationTimeBefore(now);

        notificationService.push(notifications);
    }

    private Notification createNotification(ShopNotificationQueue dueNotification) {
        Shop shop = dueNotification.getShop();
        User user = dueNotification.getUser();

        ShopNotificationMessage shopNotificationMessage = shop.getShopCategories().stream()
            .findFirst()
            .map(ShopCategoryMap::getShopCategory)
            .map(shopCategory -> shopCategory.getMainCategory().getNotificationMessage())
            .orElseThrow(() -> NotificationMessageNotFoundException.withDetail("shopId: " + shop.getId()));

        return notificationFactory.generateReviewPromptNotification(
            SHOP,
            dueNotification.getShop().getId(),
            shop.getName(),
            shopNotificationMessage.getTitle(),
            shopNotificationMessage.getContent(),
            user
        );
    }
}
