package in.koreatech.koin.domain.shop.cache;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import in.koreatech.koin.common.event.ShopsCacheRefreshEvent;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Profile("!test")
public class ShopsCacheRefreshListener {

    private final ShopsCacheService shopsCacheService;

    @EventListener(ApplicationReadyEvent.class)
    public void initCache() {
        shopsCacheService.refreshShopsCache();
    }

    @TransactionalEventListener
    public void onShopsCacheRefresh(ShopsCacheRefreshEvent event) {
        shopsCacheService.refreshShopsCache();
    }
}
