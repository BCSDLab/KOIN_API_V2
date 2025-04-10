package in.koreatech.koin.domain.shop.cache;

import static org.springframework.transaction.event.TransactionPhase.*;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import in.koreatech.koin._common.event.ShopsCacheRefreshEvent;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ShopsCacheRefreshListener {

    private final ShopsCacheService shopsCacheService;

    @EventListener(ApplicationReadyEvent.class)
    public void initCache() {
        shopsCacheService.refreshShopsCache();
    }

    @TransactionalEventListener(phase = AFTER_COMMIT)
    public void onShopsCacheRefresh(ShopsCacheRefreshEvent event) {
        shopsCacheService.refreshShopsCache();
    }
}
