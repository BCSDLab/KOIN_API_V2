package in.koreatech.koin.domain.shop.service;

import static in.koreatech.koin.global.fcm.MobileAppPath.SHOP;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.shop.model.shop.Shop;
import in.koreatech.koin.domain.shop.model.shop.ShopNotificationMessage;
import in.koreatech.koin.domain.shop.model.shop.ShopNotificationQueue;
import in.koreatech.koin.domain.shop.repository.shop.ShopCategoryMapRepository;
import in.koreatech.koin.domain.shop.repository.shop.ShopNotificationQueueRepository;
import in.koreatech.koin.domain.shop.repository.shop.ShopRepository;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.repository.UserRepository;
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
    private final ShopRepository shopRepository;
    private final ShopCategoryMapRepository shopCategoryMapRepository;
    private final UserRepository userRepository;

    @Transactional
    public void sendDueNotifications() {
        LocalDateTime now = LocalDateTime.now();

        List<Notification> notifications = shopNotificationQueueRepository
            .findByNotificationTimeBefore(now)
            .stream()
            .map(this::createNotification)
            .toList();

        shopNotificationQueueRepository.deleteByNotificationTimeBefore(now);

        notificationService.push(notifications);
    }

    private Notification createNotification(ShopNotificationQueue dueNotification) {
        Shop shop = shopRepository.getById(dueNotification.getShopId());
        ShopNotificationMessage shopNotificationMessage =
            shopCategoryMapRepository.getNotificationMessageByShopId(dueNotification.getShopId());
        User user = userRepository.getById(dueNotification.getUserId());

        return notificationFactory.generateReviewPromptNotification(
            SHOP,
            dueNotification.getShopId(),
            shop.getName(),
            shopNotificationMessage.getTitle(),
            shopNotificationMessage.getContent(),
            user
        );
    }
}
