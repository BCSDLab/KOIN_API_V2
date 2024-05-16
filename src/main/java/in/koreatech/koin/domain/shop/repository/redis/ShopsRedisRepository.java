package in.koreatech.koin.domain.shop.repository.redis;

import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.databind.ObjectMapper;

import in.koreatech.koin.domain.shop.dto.ShopsResponse;
import lombok.Getter;

@Getter
@Repository
public class ShopsRedisRepository {

    private static final String KEY = "Shops";

    private final RedisTemplate<String, Object> redisTemplate;

    private final ObjectMapper objectMapper;

    @Autowired
    public ShopsRedisRepository(
        RedisTemplate<String, Object> redisTemplate,
        ObjectMapper objectMapper
    ) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    public void save(ShopsResponse shopsResponse) {
        redisTemplate.opsForValue().set(KEY, shopsResponse, 30, TimeUnit.SECONDS);
    }

    public ShopsResponse getShopsResponseByRedis() {
        Object data = redisTemplate.opsForValue().get(KEY);
        return objectMapper.convertValue(data, ShopsResponse.class);
    }

    public Boolean isCacheAvailable() {
        return redisTemplate.hasKey(KEY);
    }
}
