package in.koreatech.koin.domain.shop.service;

import static in.koreatech.koin.global.fcm.MobileAppPath.SHOP;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.shop.dto.shop.ShopNotificationQueryResponse;
import in.koreatech.koin.domain.shop.model.redis.ShopNotificationBuffer;
import in.koreatech.koin.domain.shop.repository.shop.ShopNotificationBufferRedisRepository;
import in.koreatech.koin.domain.shop.repository.shop.ShopRepository;

import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.repository.UserRepository;
import in.koreatech.koin.global.domain.notification.model.Notification;
import in.koreatech.koin.global.domain.notification.model.NotificationFactory;
import in.koreatech.koin.global.domain.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationScheduleService {

    private final Clock clock;
    private final ShopNotificationBufferRedisRepository shopNotificationBufferRedisRepository;
    private final NotificationFactory notificationFactory;
    private final NotificationService notificationService;
    private final ShopRepository shopRepository;
    private final UserRepository userRepository;

    @Transactional
    public void sendDueNotifications() {
        LocalDateTime now = LocalDateTime.now(clock);
        List<ShopNotificationBuffer> dueNotifications = shopNotificationBufferRedisRepository.findAllByNotificationTimeBefore(now);
        if (dueNotifications.isEmpty()) {
            return;
        }

        List<Integer> shopIds = dueNotifications.stream()
            .map(ShopNotificationBuffer::getShopId)
            .toList();
        List<Integer> userIds = dueNotifications.stream()
            .map(ShopNotificationBuffer::getStudentId)
            .toList();

        Map<Integer, ShopNotificationQueryResponse> shopNotificationQueryResponseMap
            = shopRepository.findNotificationDataBatchMap(shopIds);
        Map<Integer, User> userMap = userRepository.findAllByIdInMap(userIds);

        List<Notification> notifications = dueNotifications.stream()
            .map(dueNotification -> {
                ShopNotificationQueryResponse shopNotification = shopNotificationQueryResponseMap.get(dueNotification.getShopId());
                User user = userMap.get(dueNotification.getStudentId());
                return createNotification(shopNotification, user);
            })
            .toList();

        notificationService.push(notifications);
        shopNotificationBufferRedisRepository.deleteSentNotifications(dueNotifications);
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
