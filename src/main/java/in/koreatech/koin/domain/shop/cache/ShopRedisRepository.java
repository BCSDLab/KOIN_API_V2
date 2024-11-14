package in.koreatech.koin.domain.shop.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import in.koreatech.koin.domain.shop.cache.dto.ShopsCache;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Getter
@RequiredArgsConstructor
@Repository
public class ShopRedisRepository {

    private static final String KEY = "Shops";

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    public void save(ShopsCache shopsCache) {
        redisTemplate.opsForValue().set(KEY, shopsCache);
    }

    public ShopsCache getShopsResponseByRedis() {
        Object data = redisTemplate.opsForValue().get(KEY);
        return objectMapper.convertValue(data, ShopsCache.class);
    }

    public Boolean isCacheAvailable() {
        return redisTemplate.hasKey(KEY);
    }
}
