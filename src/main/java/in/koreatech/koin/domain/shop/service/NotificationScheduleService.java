package in.koreatech.koin.domain.shop.service;

import static in.koreatech.koin._common.model.MobileAppPath.SHOP;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.notification.model.Notification;
import in.koreatech.koin.domain.notification.model.NotificationFactory;
import in.koreatech.koin.domain.notification.service.NotificationService;
import in.koreatech.koin.domain.shop.dto.shop.ShopNotificationQueryResponse;
import in.koreatech.koin.domain.shop.model.redis.ShopReviewNotification;
import in.koreatech.koin.domain.shop.repository.shop.ShopRepository;
import in.koreatech.koin.domain.shop.repository.shop.ShopReviewNotificationRedisRepository;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationScheduleService {

    private final Clock clock;
    private final ShopReviewNotificationRedisRepository shopReviewNotificationRedisRepository;
    private final NotificationFactory notificationFactory;
    private final NotificationService notificationService;
    private final ShopRepository shopRepository;
    private final UserService userService;

    @Transactional
    public void sendDueNotifications() {
        LocalDateTime now = LocalDateTime.now(clock);
        List<ShopReviewNotification> dueNotifications = shopReviewNotificationRedisRepository.findAllByNotificationTimeBefore(
            now);
        if (dueNotifications.isEmpty()) {
            return;
        }

        Map<Integer, ShopNotificationQueryResponse> shopNotificationQueryResponseMap = getShopNotificationBatch(
            dueNotifications);
        Map<Integer, User> userMap = getUserBatch(dueNotifications);

        List<Notification> notifications = dueNotifications.stream()
            .map(dueNotification -> {
                ShopNotificationQueryResponse shopNotification = shopNotificationQueryResponseMap.get(
                    dueNotification.getShopId());
                User user = userMap.get(dueNotification.getStudentId());
                return createNotification(shopNotification, user);
            })
            .toList();

        notificationService.pushNotifications(notifications);
        shopReviewNotificationRedisRepository.deleteSentNotifications(dueNotifications);
    }

    private Map<Integer, User> getUserBatch(List<ShopReviewNotification> dueNotifications) {
        List<Integer> userIds = dueNotifications.stream()
            .map(ShopReviewNotification::getStudentId)
            .toList();
        return userService.getAllByIdInMap(userIds);
    }

    private Map<Integer, ShopNotificationQueryResponse> getShopNotificationBatch(
        List<ShopReviewNotification> dueNotifications
    ) {
        List<Integer> shopIds = dueNotifications.stream()
            .map(ShopReviewNotification::getShopId)
            .toList();
        return shopRepository.findNotificationDataBatchMap(shopIds);
    }

    private Notification createNotification(ShopNotificationQueryResponse notificationQueryResponse, User user) {
        return notificationFactory.generateReviewPromptNotification(
            SHOP,
            notificationQueryResponse.shopId(),
            notificationQueryResponse.shopName(),
            notificationQueryResponse.notificationTitle(),
            notificationQueryResponse.notificationContent(),
            user
        );
    }
}
