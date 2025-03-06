package in.koreatech.koin.socket.domain.message.repository;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import in.koreatech.koin.socket.domain.message.exception.MessageProcessingException;
import in.koreatech.koin.socket.domain.message.model.ChatMessageEntity;
import in.koreatech.koin.socket.domain.message.model.redis.ChatMessageRedisEntity;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ChatMessageRedisRepository {

    private static final int COUNTER_DIGITS = 4;
    private static final String SEPARATOR = "|";
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    public ChatMessageEntity save(ChatMessageEntity message) {
        try {
            ChatMessageRedisEntity messageEntity = ChatMessageRedisEntity.toRedisEntity(message);
            String messageJson = objectMapper.writeValueAsString(messageEntity);
            String chatRoomKey = getChatRoomKey(messageEntity.getArticleId(), messageEntity.getChatRoomId());

            String tsidKey = formatTSIDKey(messageEntity.getId());

            redisTemplate.opsForZSet().add(chatRoomKey, tsidKey + SEPARATOR + messageJson, 0);
        } catch (JsonProcessingException e) {
            throw new MessageProcessingException("메시지 저장 실패");
        }
        return message;
    }

    public List<ChatMessageEntity> findByArticleIdAndChatRoomId(Integer articleId, Integer chatRoomId) {
        String key = getChatRoomKey(articleId, chatRoomId);

        Set<String> messageJsonSet = redisTemplate.opsForZSet().reverseRange(key, 0, -1);

        return convertToMessages(messageJsonSet);
    }

    public void markAllMessagesAsRead(Integer articleId, Integer chatRoomId, Integer userId) {
        String key = getChatRoomKey(articleId, chatRoomId);
        Set<String> messageJsonSet = redisTemplate.opsForZSet().range(key, 0, -1);

        if (messageJsonSet == null || messageJsonSet.isEmpty()) {
            return;
        }

        List<String> updatedMessages = messageJsonSet.stream()
            .map(value -> {
                try {
                    String tsidKey = value.substring(0, value.indexOf(SEPARATOR));
                    String json = value.substring(value.indexOf(SEPARATOR) + 1);
                    ChatMessageRedisEntity message = objectMapper.readValue(json, ChatMessageRedisEntity.class);

                    // 자신이 보낸 메시지이거나 이미 읽은 메시지는 업데이트하지 않음
                    if (message.getUserId().equals(userId) || Boolean.TRUE.equals(message.getIsRead())) {
                        return value;
                    }

                    message.readMessage();

                    String updatedJson = objectMapper.writeValueAsString(message);
                    return tsidKey + SEPARATOR + updatedJson;
                } catch (JsonProcessingException e) {
                    throw new MessageProcessingException("메시지 업데이트 실패");
                }
            })
            .toList();

        // 기존 데이터 삭제 후 업데이트된 데이터 저장
        redisTemplate.delete(key);
        for (int i = 0; i < updatedMessages.size(); i++) {
            redisTemplate.opsForZSet().add(key, updatedMessages.get(i), 0);
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
