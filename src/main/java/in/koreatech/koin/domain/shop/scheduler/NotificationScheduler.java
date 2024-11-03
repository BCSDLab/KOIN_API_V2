package in.koreatech.koin.domain.shop.scheduler;

import static in.koreatech.koin.global.fcm.MobileAppPath.*;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

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
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationScheduler {

    private final ShopNotificationQueueRepository shopNotificationQueueRepository;
    private final NotificationFactory notificationFactory;
    private final NotificationService notificationService;
    private final ShopRepository shopRepository;
    private final ShopCategoryMapRepository shopCategoryMapRepository;
    private final UserRepository userRepository;

    @Scheduled(cron = "0 * * * * *")
    public void sendDueNotifications() {
        try {
            List<ShopNotificationQueue> dueNotifications = shopNotificationQueueRepository
                .findByNotificationTimeBefore(LocalDateTime.now());

            List<Notification> notifications = dueNotifications.stream()
                .map(this::createNotification).toList();

            notificationService.push(notifications);

            shopNotificationQueueRepository.deleteAll(dueNotifications);
        } catch (Exception e) {
            log.warn("리뷰유도 알림 전송 과정에서 오류가 발생했습니다.");
        }
    }

    private Notification createNotification(ShopNotificationQueue dueNotification) {
        Shop shop = shopRepository.getById(dueNotification.getShopId());
        ShopNotificationMessage shopNotificationMessage =
            shopCategoryMapRepository.findNotificationMessageByShopId(dueNotification.getShopId());
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
