package in.koreatech.koin.domain.notification.eventlistener;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import in.koreatech.koin.common.event.EventArticleCreateShopEvent;
import in.koreatech.koin.domain.notification.service.ShopNotificationService;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Profile("!test")
public class ShopEventListener {

    private final ShopNotificationService shopNotificationService;

    @TransactionalEventListener
    public void onShopEventCreate(EventArticleCreateShopEvent event) {
        shopNotificationService.sendShopEventCreateNotifications(
            event.shopId(),
            event.shopName(),
            event.title(),
            event.thumbnailImage()
        );
    }
}
