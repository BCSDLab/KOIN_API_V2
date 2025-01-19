package in.koreatech.koin.global.socket.domain.session.repository;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import in.koreatech.koin.global.exception.KoinIllegalStateException;
import in.koreatech.koin.global.socket.domain.session.model.UserSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
@RequiredArgsConstructor
public class UserSessionRedisRepository {

    private static final long TTL_SECONDS = 60 * 60 * 24 * 7; // 1주일

    private final ObjectMapper objectMapper;

    private final RedisTemplate<String, Object> redisTemplate;

    public void save(Integer userId, String hashKey, UserSession value) {
        try {
            String key = createKey(userId);
            String serializedValue = serialize(value);

            redisTemplate.opsForHash().put(key, hashKey, serializedValue);
            redisTemplate.expire(key, TTL_SECONDS, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("웹소켓 사용자 세션 저장 실패 userId: {}", userId, e);
            throw new KoinIllegalStateException("웹소켓 사용자 세션 저장 실패");
        }
    }

    public Optional<UserSession> findUserSession(Integer userId, String hashKey) {
        try {
            Object value = redisTemplate.opsForHash().get(createKey(userId), hashKey);
            return Optional.ofNullable(deserialize(value));
        } catch (Exception e) {
            log.error("웹소켓 사용자 세션 탐색 실패 userId: {}", userId, e);
            throw new KoinIllegalStateException("웹소켓 사용자 세션 탐색 실패");
        }
    }

    public Long getSessionTtl(Integer userId, String hashKey) {
        try {
            Long ttl = redisTemplate.getExpire(createKey(userId));
            return ttl != null ? ttl : -2L;
        } catch (Exception e) {
            log.error("웹소켓 사용자 세션 ttl 탐색 실패 userId: {}", userId, e);
            throw new KoinIllegalStateException("웹소켓 사용자 세션 ttl 탐색 실패");
        }
    }

    public boolean exists(Integer userId, String hashKey) {
        try {
            return Boolean.TRUE.equals(
                redisTemplate.opsForHash().hasKey(createKey(userId), hashKey)
            );
        } catch (Exception e) {
            log.error("웹소켓 사용자 세션 탐색 실패 userId: {}", userId, e);
            throw new KoinIllegalStateException("웹소켓 사용자 세션 탐색 실패");
        }
    }

    public void resetSessionTtl(Integer userId, String hashKey) {
        try {
            redisTemplate.expire(createKey(userId), TTL_SECONDS, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("웹소켓 사용자 세션 ttl 초기화 실패 userId: {}", userId, e);
            throw new KoinIllegalStateException("웹소켓 사용자 세션 ttl 초기화 실패");
        }
    }

    public void delete(Integer userId, String hashKey) {
        try {
            redisTemplate.opsForHash().delete(createKey(userId), hashKey);
        } catch (Exception e) {
            log.error("웹소켓 사용자 세션 삭제 실패 userId: {}", userId, e);
            throw new KoinIllegalStateException("웹소켓 사용자 세션 삭제 실패");
        }
    }


    private String createKey(Integer userId) {
        return "SocketSessionUserId:" + userId;
    }

    private String serialize(UserSession value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new KoinIllegalStateException("웹소켓 사용자 세션 직렬화 실패", e.getMessage());
        }
    }

    private UserSession deserialize(Object value) {
        if (value == null) return null;
        try {
            return objectMapper.readValue((String) value, UserSession.class);
        } catch (JsonProcessingException e) {
            throw new KoinIllegalStateException("웹소켓 사용자 세션 역 직렬화 실패", e.getMessage());
        }
    }
}
