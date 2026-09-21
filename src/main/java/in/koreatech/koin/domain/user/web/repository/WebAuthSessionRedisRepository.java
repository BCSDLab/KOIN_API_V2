package in.koreatech.koin.domain.user.web.repository;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import in.koreatech.koin.domain.user.web.model.WebAuthSession;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class WebAuthSessionRedisRepository {

    private static final String KEY_PREFIX = "webAuthSession:";
    private static final DefaultRedisScript<Long> ROTATE_SCRIPT = new DefaultRedisScript<>("""
        if redis.call('GET', KEYS[1]) == ARGV[1] then
            redis.call('SET', KEYS[1], ARGV[2], 'KEEPTTL')
            return 1
        end
        return 0
        """, Long.class);
    private static final DefaultRedisScript<Long> DELETE_SCRIPT = new DefaultRedisScript<>("""
        if redis.call('GET', KEYS[1]) == ARGV[1] then
            return redis.call('DEL', KEYS[1])
        end
        return 0
        """, Long.class);

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public void save(WebAuthSession session) {
        Duration ttl = Duration.between(Instant.now(), session.expiresAt());
        if (!Boolean.TRUE.equals(redisTemplate.opsForValue()
            .setIfAbsent(KEY_PREFIX + session.id(), serialize(session), ttl))) {
            throw new IllegalStateException("웹 로그인 세션을 생성할 수 없습니다.");
        }
    }

    public Optional<WebAuthSession> findById(String sessionId) {
        String value = redisTemplate.opsForValue().get(KEY_PREFIX + sessionId);
        if (value == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(objectMapper.readValue(value, WebAuthSession.class));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("웹 로그인 세션을 읽을 수 없습니다.");
        }
    }

    public boolean rotate(WebAuthSession previous, WebAuthSession next) {
        return Long.valueOf(1).equals(redisTemplate.execute(
            ROTATE_SCRIPT, List.of(KEY_PREFIX + previous.id()), serialize(previous), serialize(next)
        ));
    }

    public boolean delete(WebAuthSession session) {
        return Long.valueOf(1).equals(redisTemplate.execute(
            DELETE_SCRIPT, List.of(KEY_PREFIX + session.id()), serialize(session)
        ));
    }

    private String serialize(WebAuthSession session) {
        try {
            return objectMapper.writeValueAsString(session);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("웹 로그인 세션을 저장할 수 없습니다.");
        }
    }
}
