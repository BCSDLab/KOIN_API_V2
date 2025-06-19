package in.koreatech.koin.domain.club.repository.redis;

import java.util.Set;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ClubHitsRedisRepository {

    private final RedisTemplate<String, Object> redisTemplate;

    public void incrementHits(Integer clubId) {
        redisTemplate.opsForValue().increment("club:hits:" + clubId);
    }

    public Set<String> getAllHitsKeys() {
        return redisTemplate.keys("club:hits:*");
    }

    public Long getHits(String key) {
        Object value = redisTemplate.opsForValue().get(key);
        return value instanceof Long ? (Long) value : 0L;
    }

    public void deleteHitsByKeys(Set<String> keys) {
        redisTemplate.delete(keys);
    }
}
