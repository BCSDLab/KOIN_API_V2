package in.koreatech.koin.global.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import in.koreatech.koin.domain.shop.service.ShopService;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CacheShopsScheduler {

    private final ShopService shopService;

    @Scheduled(fixedRate = 30000) // 30초마다 실행
    public void refreshCache() {
        shopService.refreshShopsCache();
    }
}
