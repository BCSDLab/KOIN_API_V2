package in.koreatech.koin.admin.shop.repository.redis;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.databind.ObjectMapper;

import in.koreatech.koin.admin.shop.dto.AdminShopsResponse;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Repository
public class AdminShopsRedisRepository {

    private static final String KEY = "Shops";

    private final RedisTemplate<String, Object> redisTemplate;

    private final ObjectMapper objectMapper;

    public AdminShopsResponse getAdminShopsResponseByRedis() {
        Object data = redisTemplate.opsForValue().get(KEY);
        return objectMapper.convertValue(data, AdminShopsResponse.class);
    }

    public Boolean isCacheAvailable() {
        return redisTemplate.hasKey(KEY);
    }
}
