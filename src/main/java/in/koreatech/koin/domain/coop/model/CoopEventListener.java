package in.koreatech.koin.domain.coop.model;

import static in.koreatech.koin.global.domain.notification.model.NotificationSubscribeType.DINING_SOLD_OUT;
import static in.koreatech.koin.global.fcm.MobileAppPath.DINING;
import static in.koreatech.koin.global.fcm.MobileAppPath.SHOP;
import static org.springframework.transaction.event.TransactionPhase.AFTER_COMMIT;

import java.time.Clock;
import java.time.LocalTime;
import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

import in.koreatech.koin.domain.dining.model.Dining;
import in.koreatech.koin.domain.dining.repository.DiningRepository;
import in.koreatech.koin.domain.user.repository.UserRepository;
import in.koreatech.koin.global.domain.notification.model.NotificationFactory;
import in.koreatech.koin.global.domain.notification.repository.NotificationSubscribeRepository;
import in.koreatech.koin.global.domain.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Transactional(propagation = Propagation.REQUIRES_NEW)
public class CoopEventListener {

    private final NotificationService notificationService;
    private final NotificationSubscribeRepository notificationSubscribeRepository;
    private final NotificationFactory notificationFactory;

    @TransactionalEventListener(phase = AFTER_COMMIT)
    public void onDiningSoldOutRequest(String place) {
        var notifications = notificationSubscribeRepository.findAllBySubscribeType(DINING_SOLD_OUT).stream()
            .filter(subscribe -> subscribe.getUser().getDeviceToken() != null)
            .map(subscribe -> notificationFactory.generateSoldOutNotification(
                DINING,
                place,
                subscribe.getUser()
            )).toList();
        notificationService.push(notifications);
    }
}
