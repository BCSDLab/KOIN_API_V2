package in.koreatech.koin.domain.shop.cache;

import in.koreatech.koin.domain.shop.cache.dto.ShopsCache;
import in.koreatech.koin.domain.shop.repository.shop.ShopRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class ShopsCacheService {

    private final ShopRepository shopRepository;
    private final ShopRedisRepository shopRedisRepository;

    public ShopsCache findAllShopCache() {
        if (shopRedisRepository.isCacheAvailable()) {
            return shopRedisRepository.getShopsResponseByRedis();
        }
        return refreshShopsCache();
    }

    public ShopsCache refreshShopsCache() {
        ShopsCache shopsCache = ShopsCache.from(shopRepository.findAll());
        shopRedisRepository.save(shopsCache);
        return shopsCache;
    }
}
