package in.koreatech.koin.domain.community.lostitem.chatmessage.repository;

import java.time.Duration;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ChatRoomPollingSessionRedisRepository {

    private final RedisTemplate<String, String> redisTemplate;

    private static final String KEY_PREFIX = "chat:article:";
    private static final String ROOM_SUFFIX = ":room:";
    private static final String USER_SUFFIX = ":user:";
    private static final Duration DEFAULT_TTL = Duration.ofSeconds(20);

    public void markUserOnline(Integer articleId, Integer chatRoomId, Integer userId) {
        String key = generateKey(articleId, chatRoomId, userId);
        redisTemplate.opsForValue().set(key, "online", DEFAULT_TTL);
    }

    public boolean isUserOnline(Integer articleId, Integer chatRoomId, Integer userId) {
        String key = generateKey(articleId, chatRoomId, userId);
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    public void markUserOffline(Integer articleId, Integer chatRoomId, Integer userId) {
        String key = generateKey(articleId, chatRoomId, userId);
        redisTemplate.delete(key);
    }

    private String generateKey(Integer articleId, Integer chatRoomId, Integer userId) {
        return KEY_PREFIX + articleId + ROOM_SUFFIX + chatRoomId + USER_SUFFIX + userId;
    }
}
