package in.koreatech.koin.domain.shop.service;

import static in.koreatech.koin.global.fcm.MobileAppPath.SHOP;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.shop.exception.NotificationMessageNotFoundException;
import in.koreatech.koin.domain.shop.model.shop.Shop;
import in.koreatech.koin.domain.shop.model.shop.ShopCategoryMap;
import in.koreatech.koin.domain.shop.model.shop.ShopNotificationBuffer;
import in.koreatech.koin.domain.shop.model.shop.ShopNotificationMessage;
import in.koreatech.koin.domain.shop.repository.shop.ShopNotificationBufferRepository;
import in.koreatech.koin.domain.user.model.User;

import in.koreatech.koin.global.domain.notification.model.Notification;
import in.koreatech.koin.global.domain.notification.model.NotificationFactory;
import in.koreatech.koin.global.domain.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationScheduleService {

    private final Clock clock;
    private final ShopNotificationBufferRepository shopNotificationBufferRepository;
    private final NotificationFactory notificationFactory;
    private final NotificationService notificationService;

    @Transactional
    public void sendDueNotifications() {
        LocalDateTime now = LocalDateTime.now(clock);
        List<ShopNotificationBuffer> dueNotifications = shopNotificationBufferRepository.findByNotificationTimeBefore(now);
        if (dueNotifications.isEmpty()) {
            return;
        }

        List<Notification> notifications = dueNotifications.stream()
            .map(this::createNotification)
            .toList();
        shopNotificationBufferRepository.deleteByNotificationTimeBefore(now);

        notificationService.push(notifications);
    }

    private Notification createNotification(ShopNotificationBuffer dueNotification) {
        Shop shop = dueNotification.getShop();
        User user = dueNotification.getUser();

        ShopNotificationMessage shopNotificationMessage = shop.getShopCategories().stream()
            .findFirst()
            .map(ShopCategoryMap::getShopCategory)
            .map(shopCategory -> shopCategory.getParentCategory().getNotificationMessage())
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
