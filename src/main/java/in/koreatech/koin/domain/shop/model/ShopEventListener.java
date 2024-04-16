package in.koreatech.koin.domain.shop.model;

import static in.koreatech.koin.global.fcm.MobileAppPath.SHOP;
import static org.springframework.transaction.event.TransactionPhase.AFTER_COMMIT;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

import in.koreatech.koin.domain.ownershop.EventArticleCreateEvent;
import in.koreatech.koin.domain.user.repository.UserRepository;
import in.koreatech.koin.global.domain.notification.model.NotificationFactory;
import in.koreatech.koin.global.domain.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Transactional(propagation = Propagation.REQUIRES_NEW)
public class ShopEventListener {

    private final NotificationService notificationService;
    private final NotificationFactory notificationFactory;
    private final UserRepository userRepository;

    @TransactionalEventListener(phase = AFTER_COMMIT)
    public void onShopEventCreate(EventArticleCreateEvent event) {
        var notifications = userRepository.findAllByDeviceTokenIsNotNull()
            .stream()
            .map(user -> notificationFactory.generateShopEventCreateNotification(
                SHOP,
                event.shopName(),
                event.title(),
                user
            )).toList();
        notificationService.push(notifications);
    }
}
