package in.koreatech.koin.domain.notification.service;

import static in.koreatech.koin.common.model.MobileAppPath.DINING;
import static in.koreatech.koin.domain.notification.model.NotificationSubscribeType.DINING_IMAGE_UPLOAD;
import static in.koreatech.koin.domain.notification.model.NotificationSubscribeType.DINING_SOLD_OUT;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.dining.model.DiningType;
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

    public void sendDiningSoldOutNotifications(Integer diningId, String place, DiningType diningType) {
        NotificationDetailSubscribeType detailType = NotificationDetailSubscribeType.from(diningType);
        var notifications = notificationSubscribeRepository.findAllBySubscribeTypeAndDetailType(DINING_SOLD_OUT, detailType)
            .stream()
            .map(subscribe -> notificationFactory.generateSoldOutNotification(
                DINING,
                diningId,
                place,
                subscribe.getUser()
            ))
            .toList();
        notificationService.pushNotifications(notifications);
    }

    public void sendDiningImageUploadNotifications(int id, String imageUrl) {
        var notifications = notificationSubscribeRepository
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
