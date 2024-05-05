package in.koreatech.koin.domain.coop.model;

import static in.koreatech.koin.global.domain.notification.model.NotificationSubscribeType.DINING_SOLD_OUT;
import static in.koreatech.koin.global.fcm.MobileAppPath.HOME;
import static org.springframework.transaction.event.TransactionPhase.AFTER_COMMIT;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

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
    private final NotificationSubscribeRepository notificationSubscribeRepository;
    private final NotificationFactory notificationFactory;

    @TransactionalEventListener(phase = AFTER_COMMIT)
    public void onDiningSoldOutRequest(DiningSoldOutEvent event) {
        //TODO : detailType 필터링 추가
        var notifications = notificationSubscribeRepository.findAllBySubscribeType(DINING_SOLD_OUT).stream()
            .map(subscribe -> userRepository.getById(subscribe.getUser().getId()))
            .filter(user -> user.getDeviceToken() != null)
            .map(user -> notificationFactory.generateSoldOutNotification(
                HOME,
                event.place(),
                user
            )).toList();
        notificationService.push(notifications);
    }
}
