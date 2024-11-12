package in.koreatech.koin.global.scheduler;

import in.koreatech.koin.domain.shop.cache.ShopsCacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CacheShopsScheduler {
    private final ShopsCacheService shopsCacheService;

    @Scheduled(fixedRate = 30000)
    public void refreshCache() {
        shopsCacheService.refreshShopsCache();
    }
}
