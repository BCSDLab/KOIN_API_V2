package in.koreatech.koin.domain.community.lostitem.chatmessage.repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.domain.Range;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import in.koreatech.koin.domain.community.lostitem.chatmessage.exception.MessageProcessingException;
import in.koreatech.koin.domain.community.lostitem.chatmessage.model.ChatMessageEntity;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ChatMessageRedisRepository {

    private static final int COUNTER_DIGITS = 4;
    private static final String SEPARATOR = "|";
    private static final DefaultRedisScript<Long> MARK_READ_SCRIPT = new DefaultRedisScript<>("""
        local updated = 0
        for i = 1, #ARGV, 2 do
            local score = redis.call('ZSCORE', KEYS[1], ARGV[i])
            if score then
                redis.call('ZADD', KEYS[1], score, ARGV[i + 1])
                redis.call('ZREM', KEYS[1], ARGV[i])
                updated = updated + 1
            end
        end
        return updated
        """, Long.class);
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    public void save(ChatMessageEntity messageEntity) {
        try {
            String messageJson = objectMapper.writeValueAsString(messageEntity);
            String chatRoomKey = getChatRoomKey(messageEntity.getArticleId(), messageEntity.getChatRoomId());

            String tsidKey = formatTSIDKey(messageEntity.getId());

            redisTemplate.opsForZSet().add(chatRoomKey, tsidKey + SEPARATOR + messageJson, 0);
        } catch (JsonProcessingException e) {
            throw new MessageProcessingException("메시지 저장 실패");
        }
    }

    public List<ChatMessageEntity> findByArticleIdAndChatRoomId(Integer articleId, Integer chatRoomId) {
        String key = getChatRoomKey(articleId, chatRoomId);

        Set<String> messageJsonSet = redisTemplate.opsForZSet().reverseRange(key, 0, -1);

        return convertToMessages(messageJsonSet);
    }

    public Optional<ChatMessageEntity> findByArticleIdAndChatRoomIdAndId(
        Integer articleId, Integer chatRoomId, Long messageId
    ) {
        if (messageId == null || messageId < 10_000) {
            return Optional.empty();
        }
        String tsidKey = formatTSIDKey(messageId);
        // 모든 member의 score는 0이다. '|' 다음 ASCII 문자인 '}'를 배타 상한으로 사용한다.
        Set<String> values = redisTemplate.opsForZSet().rangeByLex(getChatRoomKey(articleId, chatRoomId),
            Range.rightOpen(tsidKey + SEPARATOR, tsidKey + "}"));
        if (values == null || values.isEmpty()) {
            return Optional.empty();
        }
        return values.stream()
            .map(value -> {
                try {
                    return objectMapper.readValue(value.substring(value.indexOf(SEPARATOR) + 1), ChatMessageEntity.class);
                } catch (JsonProcessingException exception) {
                    throw new MessageProcessingException("메시지 조회 실패");
                }
            })
            .filter(Objects::nonNull)
            .filter(message -> Objects.equals(message.getId(), messageId))
            .filter(message -> Objects.equals(message.getArticleId(), articleId))
            .filter(message -> Objects.equals(message.getChatRoomId(), chatRoomId))
            .findFirst();
    }

    public void markAllMessagesAsRead(Integer articleId, Integer chatRoomId, Integer userId) {
        String key = getChatRoomKey(articleId, chatRoomId);
        Set<String> messageJsonSet = redisTemplate.opsForZSet().range(key, 0, -1);

        if (messageJsonSet == null || messageJsonSet.isEmpty()) {
            return;
        }

        List<String> replacements = new ArrayList<>();
        for (String value : messageJsonSet) {
            try {
                String tsidKey = value.substring(0, value.indexOf(SEPARATOR));
                String json = value.substring(value.indexOf(SEPARATOR) + 1);
                ChatMessageEntity message = objectMapper.readValue(json, ChatMessageEntity.class);
                if (message.getUserId().equals(userId) || Boolean.TRUE.equals(message.getIsRead())) {
                    continue;
                }
                message.readMessage();
                replacements.add(value);
                replacements.add(tsidKey + SEPARATOR + objectMapper.writeValueAsString(message));
            } catch (JsonProcessingException e) {
                throw new MessageProcessingException("메시지 업데이트 실패");
            }
        }
        if (!replacements.isEmpty()) {
            // 원본 member가 그대로 있을 때만 교체한다. 먼저 추가해 TTL을 유지하고 방 전체 삭제를 피한다.
            redisTemplate.execute(MARK_READ_SCRIPT, List.of(key), replacements.toArray());
        }
    }

    private String getChatRoomKey(Integer articleId, Integer roomId) {
        return "article:" + articleId +":chatroom:" + roomId + ":message";
    }

    private String formatTSIDKey(long tsid) {
        String tsidStr = String.valueOf(tsid);

        String timestamp = tsidStr.substring(0, tsidStr.length() - COUNTER_DIGITS);
        String counter = tsidStr.substring(tsidStr.length() - COUNTER_DIGITS);

        return timestamp + ":" + counter;
    }

    private List<ChatMessageEntity> convertToMessages(Set<String> messageJsonSet) {
        if (messageJsonSet == null || messageJsonSet.isEmpty()) {
            return Collections.emptyList();
        }

        return messageJsonSet.stream()
            .map(value -> {
                try {
                    String json = value.substring(value.indexOf(SEPARATOR) + 1);
                    return objectMapper.readValue(json, ChatMessageEntity.class);
                } catch (JsonProcessingException e) {
                    return null;
                }
            })
            .filter(Objects::nonNull)
            .sorted(Comparator.comparing(ChatMessageEntity::getId))
            .toList();
    }
}
