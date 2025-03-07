package in.koreatech.koin.domain.coop.model;

import static in.koreatech.koin._common.domain.notification.model.NotificationSubscribeType.DINING_IMAGE_UPLOAD;
import static in.koreatech.koin._common.domain.notification.model.NotificationSubscribeType.DINING_SOLD_OUT;
import static in.koreatech.koin._common.integration.fcm.MobileAppPath.DINING;
import static org.springframework.transaction.event.TransactionPhase.AFTER_COMMIT;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

import in.koreatech.koin.domain.coop.repository.DiningSoldOutCacheRepository;
import in.koreatech.koin._common.domain.notification.model.NotificationDetailSubscribeType;
import in.koreatech.koin._common.domain.notification.model.NotificationFactory;
import in.koreatech.koin._common.domain.notification.repository.NotificationSubscribeRepository;
import in.koreatech.koin._common.domain.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Transactional(propagation = Propagation.REQUIRES_NEW)
public class CoopEventListener {

    private final NotificationService notificationService;
    private final NotificationSubscribeRepository notificationSubscribeRepository;
    private final NotificationFactory notificationFactory;
    private final DiningSoldOutCacheRepository diningSoldOutCacheRepository;

    @TransactionalEventListener(phase = AFTER_COMMIT)
    public void onDiningSoldOutRequest(DiningSoldOutEvent event) {
        diningSoldOutCacheRepository.save(DiningSoldOutCache.from(event.place()));
        NotificationDetailSubscribeType detailType = NotificationDetailSubscribeType.from(event.diningType());
        var notifications = notificationSubscribeRepository
            .findAllBySubscribeTypeAndDetailType(DINING_SOLD_OUT, null).stream()
            .filter(subscribe -> notificationSubscribeRepository.findByUserIdAndSubscribeTypeAndDetailType(
                subscribe.getUser().getId(), DINING_SOLD_OUT, detailType).isPresent()
            )
            .filter(subscribe -> subscribe.getUser().getDeviceToken() != null)
            .map(subscribe -> notificationFactory.generateSoldOutNotification(
                DINING,
                event.id(),
                event.place(),
                subscribe.getUser()
            )).toList();
        notificationService.push(notifications);
    }

    @TransactionalEventListener(phase = AFTER_COMMIT)
    public void onDiningImageUploadRequest(DiningImageUploadEvent event) {
        var notifications = notificationSubscribeRepository
            .findAllBySubscribeTypeAndDetailType(DINING_IMAGE_UPLOAD, null).stream()
            .filter(subscribe -> subscribe.getUser().getDeviceToken() != null)
            .map(subscribe -> notificationFactory.generateDiningImageUploadNotification(
                DINING,
                event.id(),
                event.imageUrl(),
                subscribe.getUser()
            )).toList();

        notificationService.push(notifications);
    }
}
