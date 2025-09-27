package in.koreatech.koin.domain.notification.eventlistener;

import static in.koreatech.koin.common.model.MobileAppPath.DINING;
import static in.koreatech.koin.domain.notification.model.NotificationSubscribeType.DINING_IMAGE_UPLOAD;
import static in.koreatech.koin.domain.notification.model.NotificationSubscribeType.DINING_SOLD_OUT;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import in.koreatech.koin.common.event.DiningImageUploadEvent;
import in.koreatech.koin.common.event.DiningSoldOutEvent;
import in.koreatech.koin.domain.notification.model.NotificationDetailSubscribeType;
import in.koreatech.koin.domain.notification.model.NotificationFactory;
import in.koreatech.koin.domain.notification.repository.NotificationSubscribeRepository;
import in.koreatech.koin.domain.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Profile("!test")
public class CoopEventListener { // TODO : 리팩터링 필요 (비즈니스로직 제거 및 알림 책임만 갖도록)

    private final NotificationService notificationService;
    private final NotificationSubscribeRepository notificationSubscribeRepository;
    private final NotificationFactory notificationFactory;

    @TransactionalEventListener
    public void onDiningSoldOutRequest(DiningSoldOutEvent event) {
        NotificationDetailSubscribeType detailType = NotificationDetailSubscribeType.from(event.diningType());
        var notifications = notificationSubscribeRepository.findAllSoldOutSubscribers(DINING_SOLD_OUT, detailType)
            .stream()
            .map(subscribe -> notificationFactory.generateSoldOutNotification(
                DINING,
                event.id(),
                event.place(),
                subscribe.getUser()
            ))
            .toList();
        notificationService.pushNotifications(notifications);
    }

    @TransactionalEventListener
    public void onDiningImageUploadRequest(DiningImageUploadEvent event) {
        var notifications = notificationSubscribeRepository
            .findAllBySubscribeTypeAndDetailTypeIsNull(DINING_IMAGE_UPLOAD).stream()
            .filter(subscribe -> subscribe.getUser().getDeviceToken() != null)
            .map(subscribe -> notificationFactory.generateDiningImageUploadNotification(
                DINING,
                event.id(),
                event.imageUrl(),
                subscribe.getUser()
            )).toList();

        notificationService.pushNotifications(notifications);
    }
}
