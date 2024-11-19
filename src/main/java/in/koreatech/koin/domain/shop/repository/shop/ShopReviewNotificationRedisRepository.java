package in.koreatech.koin.domain.shop.repository.shop;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.databind.ObjectMapper;

import in.koreatech.koin.domain.shop.model.redis.ShopReviewNotification;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@Repository
public class ShopReviewNotificationRedisRepository {

    private static final String KEY = "ShopReviewNotifications";

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    public void save(ShopReviewNotification shopReviewNotification, double score) {
        redisTemplate.opsForZSet().add(KEY, shopReviewNotification, score);
    }

    public List<ShopReviewNotification> findAllByNotificationTimeBefore(LocalDateTime notificationTime) {
        double currentTimeScore = notificationTime.toEpochSecond(ZoneOffset.UTC);
        Set<Object> rawResults = redisTemplate.opsForZSet().rangeByScore(KEY, Double.NEGATIVE_INFINITY, currentTimeScore);

        return Optional.ofNullable(rawResults)
            .orElse(Set.of())
            .stream()
            .map(result -> objectMapper.convertValue(result, ShopReviewNotification.class))
            .toList();
    }

    public void deleteSentNotifications(List<ShopReviewNotification> notifications) {
        redisTemplate.opsForZSet().remove(KEY, notifications.toArray());
    }
}
