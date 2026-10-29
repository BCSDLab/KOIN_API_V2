package in.koreatech.koin.domain.notification.service;

import static in.koreatech.koin.common.model.MobileAppPath.DINING;
import static in.koreatech.koin.domain.notification.model.NotificationSubscribeType.DINING_IMAGE_UPLOAD;
import static in.koreatech.koin.domain.notification.model.NotificationSubscribeType.DINING_SOLD_OUT;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;

import in.koreatech.koin.domain.coop.model.DiningSoldOutCache;
import in.koreatech.koin.domain.coop.repository.DiningSoldOutCacheRepository;
import in.koreatech.koin.domain.coopshop.model.CoopShopType;
import in.koreatech.koin.domain.coopshop.service.CoopShopService;
import in.koreatech.koin.domain.dining.model.Dining;
import in.koreatech.koin.domain.dining.model.DiningType;
import in.koreatech.koin.domain.dining.repository.DiningRepository;
import in.koreatech.koin.domain.notification.model.Notification;
import in.koreatech.koin.domain.notification.model.NotificationDetailSubscribeType;
import in.koreatech.koin.domain.notification.model.NotificationFactory;
import in.koreatech.koin.domain.notification.repository.NotificationSubscribeRepository;
import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CoopNotificationService {

    private final NotificationSubscribeRepository notificationSubscribeRepository;
    private final NotificationFactory notificationFactory;
    private final NotificationService notificationService;
    private final DiningSoldOutCacheRepository diningSoldOutCacheRepository;
    private final DiningRepository diningRepository;
    private final CoopShopService coopShopService;
    private final Clock clock;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void sendDiningSoldOutNotifications(Integer diningId, String place, DiningType diningType) {
        Dining dining = diningRepository.getById(diningId);
        LocalDateTime now = LocalDateTime.now(clock.withZone(ZoneId.of("Asia/Seoul")));
        if (dining.getSoldOut() == null || !dining.getDate().equals(now.toLocalDate())
            || !coopShopService.getIsOpened(now, CoopShopType.CAFETERIA, diningType, false)
            || diningSoldOutCacheRepository.findById(place).isPresent()) {
            return;
        }
        NotificationDetailSubscribeType detailType = NotificationDetailSubscribeType.from(diningType);
        List<Notification> notifications = notificationSubscribeRepository.findAllBySubscribeTypeAndDetailType(
                DINING_SOLD_OUT, detailType)
            .stream()
            .map(subscribe -> notificationFactory.generateSoldOutNotification(
                DINING,
                diningId,
                place,
                subscribe.getUser()
            ))
            .toList();
        notificationService.pushNotifications(notifications);
        diningSoldOutCacheRepository.save(DiningSoldOutCache.from(place));
    }

    public void sendDiningImageUploadNotifications(int id, String imageUrl) {
        List<Notification> notifications = notificationSubscribeRepository
            .findAllBySubscribeTypeAndDetailTypeIsNull(DINING_IMAGE_UPLOAD).stream()
            .filter(subscribe -> subscribe.getUser().getDeviceToken() != null)
            .map(subscribe -> notificationFactory.generateDiningImageUploadNotification(
                DINING,
                id,
                imageUrl,
                subscribe.getUser()
            )).toList();

        notificationService.pushNotifications(notifications);
    }
}
