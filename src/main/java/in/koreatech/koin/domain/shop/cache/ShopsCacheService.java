package in.koreatech.koin.domain.shop.cache;

import in.koreatech.koin.domain.shop.cache.dto.ShopsCache;
import in.koreatech.koin.domain.shop.repository.shop.ShopRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class ShopsCacheService {

    private final ShopRepository shopRepository;
    private final ShopRedisRepository shopRedisRepository;

    public ShopsCache findAllShopCache() {
        if (shopRedisRepository.isCacheAvailable()) {
            return shopRedisRepository.getShopsResponseByRedis();
        }
        return ShopsCache.from(shopRepository.findAll());
    }
}
