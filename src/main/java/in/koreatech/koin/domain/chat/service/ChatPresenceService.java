package in.koreatech.koin.domain.chat.service;

import java.util.List;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import in.koreatech.koin.domain.chat.model.ChatRoomKey;
import in.koreatech.koin.domain.chat.model.ChatDomainType;
import in.koreatech.koin.domain.community.lostitem.chatmessage.repository.ChatRoomPollingSessionRedisRepository;
import in.koreatech.koin.global.code.ApiResponseCode;
import in.koreatech.koin.global.exception.CustomException;
import in.koreatech.koin.socket.session.model.WebSocketUserSessionStatus;
import in.koreatech.koin.socket.session.service.WebSocketUserSessionService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatPresenceService {
    // Heartbeats renew each session for 30 seconds; clients should touch at least every 15 seconds.
    private static final long SESSION_TTL_MILLIS = 30_000;
    private static final DefaultRedisScript<Long> PRESENCE = new DefaultRedisScript<>("""
        local time = redis.call('TIME')
        local now = tonumber(time[1]) * 1000 + math.floor(tonumber(time[2]) / 1000)
        redis.call('ZREMRANGEBYSCORE', KEYS[1], '-inf', now)
        if ARGV[1] == 'touch' then
            redis.call('ZADD', KEYS[1], now + tonumber(ARGV[3]), ARGV[2])
            redis.call('PEXPIRE', KEYS[1], ARGV[3])
        elseif ARGV[1] == 'leave' then
            redis.call('ZREM', KEYS[1], ARGV[2])
        end
        return redis.call('ZCARD', KEYS[1])
        """, Long.class);

    private final StringRedisTemplate redis;
    private final ChatRoomPollingSessionRedisRepository pollingSessions;
    private final WebSocketUserSessionService legacySessions;

    public void touchPolling(ChatRoomKey room, Integer userId) {
        validateUserId(userId);
        pollingSessions.markUserOnline(room, userId);
    }

    public void leavePolling(ChatRoomKey room, Integer userId) {
        validateUserId(userId);
        pollingSessions.markUserOffline(room, userId);
    }

    /** Caller supplies an authorized user and a server-scoped WebSocket session, never a token. */
    public void touch(ChatRoomKey room, Integer userId, String sessionId) {
        validateSession(sessionId);
        execute(room, userId, "touch", sessionId);
    }

    public void leave(ChatRoomKey room, Integer userId, String sessionId) {
        validateSession(sessionId);
        execute(room, userId, "leave", sessionId);
    }

    public boolean isOnline(ChatRoomKey room, Integer userId) {
        validateUserId(userId);
        if (pollingSessions.isUserOnline(room, userId)) {
            return true;
        }
        if (room.domainType() == ChatDomainType.LOST_ITEM) {
            if (pollingSessions.isUserOnline(room.contextId(), room.chatRoomId(), userId)
                || legacySessions.read(userId).filter(session ->
                    session.getStatus() == WebSocketUserSessionStatus.ACTIVE_CHAT_ROOM
                        && java.util.Objects.equals(room.contextId().longValue(), session.getCurrentArticleId())
                        && java.util.Objects.equals(room.chatRoomId().longValue(), session.getCurrentChatRoomId())).isPresent()) {
                return true;
            }
        }
        Long sessions = execute(room, userId, "read", "");
        return sessions != null && sessions > 0;
    }

    private Long execute(ChatRoomKey room, Integer userId, String operation, String sessionId) {
        if (room == null) {
            throw CustomException.of(ApiResponseCode.ILLEGAL_ARGUMENT);
        }
        validateUserId(userId);
        return redis.execute(PRESENCE, List.of("chat:presence:v1:" + room.encoded() + ":" + userId),
            operation, sessionId, Long.toString(SESSION_TTL_MILLIS));
    }

    private void validateUserId(Integer userId) {
        if (userId == null || userId < 1) {
            throw CustomException.of(ApiResponseCode.ILLEGAL_ARGUMENT);
        }
    }

    private void validateSession(String sessionId) {
        if (sessionId == null || sessionId.length() > 128 || !sessionId.matches("ws:[A-Za-z0-9_-]+")) {
            throw CustomException.of(ApiResponseCode.ILLEGAL_ARGUMENT);
        }
    }
}
