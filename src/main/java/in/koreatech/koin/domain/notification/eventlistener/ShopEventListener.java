package in.koreatech.koin.domain.notification.eventlistener;

import static in.koreatech.koin._common.model.MobileAppPath.SHOP;
import static in.koreatech.koin.domain.notification.model.NotificationSubscribeType.SHOP_EVENT;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import in.koreatech.koin._common.event.EventArticleCreateShopEvent;
import in.koreatech.koin.domain.notification.model.Notification;
import in.koreatech.koin.domain.notification.model.NotificationFactory;
import in.koreatech.koin.domain.notification.repository.NotificationSubscribeRepository;
import in.koreatech.koin.domain.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ShopEventListener { // TODO : 리팩터링 필요 (비즈니스로직 제거 및 알림 책임만 갖도록)

    private final NotificationService notificationService;
    private final NotificationFactory notificationFactory;
    private final NotificationSubscribeRepository notificationSubscribeRepository;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void onShopEventCreate(EventArticleCreateShopEvent event) {
        List<Notification> notifications = notificationSubscribeRepository
            .findAllBySubscribeTypeAndDetailTypeIsNull(SHOP_EVENT)
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
