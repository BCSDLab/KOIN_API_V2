package in.koreatech.koin.domain.shop.repository.shop;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.databind.ObjectMapper;

import in.koreatech.koin.domain.shop.model.redis.ShopNotificationBuffer;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@Repository
public class ShopNotificationBufferRedisRepository {

    private static final String KEY = "ShopNotificationBuffers";

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    public void save(ShopNotificationBuffer shopNotificationBuffer, double score) {
        redisTemplate.opsForZSet().add(KEY, shopNotificationBuffer, score);
    }

    public List<ShopNotificationBuffer> findAllByNotificationTimeBefore(LocalDateTime notificationTime) {
        double currentTimeScore = notificationTime.toEpochSecond(ZoneOffset.UTC);
        Set<Object> rawResults = redisTemplate.opsForZSet().rangeByScore(KEY, Double.NEGATIVE_INFINITY, currentTimeScore);

        return Optional.ofNullable(rawResults)
            .orElse(Set.of())
            .stream()
            .map(result -> objectMapper.convertValue(result, ShopNotificationBuffer.class))
            .toList();
    }

    public void deleteSentNotifications(List <ShopNotificationBuffer> buffers) {
        redisTemplate.opsForZSet().remove(KEY, buffers.toArray());
    }
}
