package in.koreatech.koin.domain.shop.model.shop;

import static in.koreatech.koin.global.domain.notification.model.NotificationSubscribeType.SHOP_EVENT;
import static in.koreatech.koin.global.fcm.MobileAppPath.SHOP;
import static org.springframework.transaction.event.TransactionPhase.AFTER_COMMIT;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

import in.koreatech.koin.domain.ownershop.dto.EventArticleCreateShopEvent;
import in.koreatech.koin.global.domain.notification.model.Notification;
import in.koreatech.koin.global.domain.notification.model.NotificationFactory;
import in.koreatech.koin.global.domain.notification.repository.NotificationSubscribeRepository;
import in.koreatech.koin.global.domain.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Transactional(propagation = Propagation.REQUIRES_NEW)
public class ShopEventListener {

    private final NotificationService notificationService;
    private final NotificationFactory notificationFactory;
    private final NotificationSubscribeRepository notificationSubscribeRepository;

    @TransactionalEventListener(phase = AFTER_COMMIT)
    public void onShopEventCreate(EventArticleCreateShopEvent event) {
        List<Notification> notifications = notificationSubscribeRepository
            .findAllBySubscribeTypeAndDetailType(SHOP_EVENT, null)
            .stream()
            .filter(subscribe -> subscribe.getUser().getDeviceToken() != null)
            .map(subscribe -> notificationFactory.generateShopEventCreateNotification(
                SHOP,
                event.shopId(),
                event.thumbnailImage(),
                event.shopName(),
                event.title(),
                subscribe.getUser()
            )).toList();
        notificationService.push(notifications);
    }
}
