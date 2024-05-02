package in.koreatech.koin.domain.coop.model;

import static in.koreatech.koin.global.domain.notification.model.NotificationSubscribeType.DINING_SOLD_OUT;
import static in.koreatech.koin.global.fcm.MobileAppPath.DINING;
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
    private final UserRepository userRepository;
    private final DiningRepository diningRepository;
    private final NotificationSubscribeRepository notificationSubscribeRepository;
    private final NotificationFactory notificationFactory;
    private final Clock clock;

    @TransactionalEventListener(phase = AFTER_COMMIT)
    public void onDiningSoldOutRequest(DiningSoldOutEvent event) {
        Dining dining = diningRepository.getById(event.id());
        Map<String, LocalTime> diningTime = dining.getType().getDiningTime();
        LocalTime now = LocalTime.now(clock);

        if (dining.isSent()) {
            return;
        }

        if (now.isBefore(diningTime.get("startTime")) || now.isAfter(diningTime.get("endTime"))) {
            return;
        }

        var notifications = notificationSubscribeRepository.findAllBySubscribeType(DINING_SOLD_OUT).stream()
            .map(subscribe -> userRepository.getById(subscribe.getUser().getId()))
            .filter(user -> user.getDeviceToken() != null)
            .map(user -> notificationFactory.generateSoldOutNotification(
                DINING,
                event.place(),
                user
            )).toList();
        notificationService.push(notifications);
        dining.setIsSent(true);
    }
}
