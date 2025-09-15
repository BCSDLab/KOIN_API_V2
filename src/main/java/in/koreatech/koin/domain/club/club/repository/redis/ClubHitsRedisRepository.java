package in.koreatech.koin.domain.club.club.repository.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.Set;

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

    public Integer getHits(String key) {
        Object value = redisTemplate.opsForValue().get(key);
        return value instanceof Integer ? (Integer) value : 0;
    }

    public void deleteHitsByKeys(Set<String> keys) {
        redisTemplate.delete(keys);
    }
}
