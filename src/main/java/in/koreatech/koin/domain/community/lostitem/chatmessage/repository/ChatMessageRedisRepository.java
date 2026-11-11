package in.koreatech.koin.domain.community.lostitem.chatmessage.repository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Objects;
import java.util.Set;

import org.redisson.api.RedissonClient;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.connection.Limit;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.f4b6a3.tsid.TsidCreator;

import in.koreatech.koin.domain.community.lostitem.chatmessage.exception.MessageProcessingException;
import in.koreatech.koin.domain.community.lostitem.chatmessage.model.ChatMessageEntity;
import in.koreatech.koin.global.code.ApiResponseCode;
import in.koreatech.koin.global.exception.CustomException;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ChatMessageRedisRepository {

    private static final int COUNTER_DIGITS = 4;
    private static final String SEPARATOR = "|";
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    private final RedissonClient redissonClient;
    private static final DefaultRedisScript<Long> ADVANCE_READ = new DefaultRedisScript<>("""
        local previous = redis.call('HGET', KEYS[1], ARGV[1])
        local next = ARGV[2]
        if not previous or #next > #previous or (#next == #previous and next > previous) then
            redis.call('HSET', KEYS[1], ARGV[1], next)
            return 1
        end
        return 0
        """, Long.class);

    private static final DefaultRedisScript<Long> REPLACE_MESSAGE = new DefaultRedisScript<>("""
        local score = redis.call('ZSCORE', KEYS[1], ARGV[1])
        if not score then return 0 end
        redis.call('ZREM', KEYS[1], ARGV[1])
        redis.call('ZADD', KEYS[1], score, ARGV[2])
        return 1
        """, Long.class);

    public ChatMessageEntity save(ChatMessageEntity messageEntity) {
        String roomKey = getChatRoomKey(messageEntity.getArticleId(), messageEntity.getChatRoomId());
        var lock = redissonClient.getLock(roomKey + ":save-lock");
        lock.lock();
        try {
            String sequence = redisTemplate.opsForValue().get(roomKey + ":sequence");
            long previous = sequence == null
                ? convertToMessages(roomKey, redisTemplate.opsForZSet().reverseRange(roomKey, 0, -1)).stream()
                    .mapToLong(ChatMessageEntity::getId).max().orElse(0L)
                : Long.parseLong(sequence);
            long next = Math.max(Math.incrementExact(previous), TsidCreator.getTsid256().toLong());
            redisTemplate.opsForValue().set(roomKey + ":sequence", Long.toString(next));
            messageEntity = messageEntity.toBuilder().id(next).createdAt(LocalDateTime.now()).build();
            String messageJson = objectMapper.writeValueAsString(messageEntity);
            String chatRoomKey = getChatRoomKey(messageEntity.getArticleId(), messageEntity.getChatRoomId());

            String tsidKey = formatTSIDKey(messageEntity.getId());

            redisTemplate.opsForZSet().add(chatRoomKey, tsidKey + SEPARATOR + messageJson, 0);
            return messageEntity;
        } catch (JsonProcessingException e) {
            throw new MessageProcessingException("메시지 저장 실패: " + roomKey, e);
        } finally {
            lock.unlock();
        }
    }

    public List<ChatMessageEntity> findByArticleIdAndChatRoomId(Integer articleId, Integer chatRoomId) {
        String key = getChatRoomKey(articleId, chatRoomId);

        Set<String> messageJsonSet = redisTemplate.opsForZSet().reverseRange(key, 0, -1);

        List<ChatMessageEntity> messages = convertToMessages(key, messageJsonSet);
        if (messages.isEmpty()) {
            return messages;
        }
        var receipts = redisTemplate.opsForHash().entries(key + ":read");
        for (var message : messages) {
            for (var receipt : receipts.entrySet()) {
                if (!receipt.getKey().toString().equals(String.valueOf(message.getUserId()))
                    && Long.parseLong(receipt.getValue().toString()) >= message.getId()) {
                    message.readMessage();
                }
            }
        }
        return messages;
    }

    public boolean markAllMessagesAsRead(Integer articleId, Integer chatRoomId, Integer userId) {
        List<ChatMessageEntity> messages = findByArticleIdAndChatRoomId(articleId, chatRoomId);
        return messages.stream().map(ChatMessageEntity::getId).max(Long::compareTo)
            .map(id -> advanceRead(articleId, chatRoomId, userId, id)).orElse(false);
    }

    public boolean markRead(Integer articleId, Integer chatRoomId, Integer userId, Long messageId) {
        if (findMessage(articleId, chatRoomId, messageId).isEmpty()) {
            throw CustomException.of(ApiResponseCode.ILLEGAL_ARGUMENT, "해당 채팅방에 존재하지 않는 메시지입니다.");
        }
        return advanceRead(articleId, chatRoomId, userId, messageId);
    }

    private Optional<ChatMessageEntity> findMessage(Integer articleId, Integer chatRoomId, Long messageId) {
        if (messageId == null || messageId < 1) {
            return Optional.empty();
        }
        String key = getChatRoomKey(articleId, chatRoomId);
        String id = messageId.toString();
        String prefix = (id.length() > COUNTER_DIGITS ? formatTSIDKey(messageId) : id) + SEPARATOR;
        Set<String> stored = redisTemplate.opsForZSet().rangeByLex(key,
            Range.closed(prefix, prefix + "\uffff"), Limit.limit().count(1));
        if ((stored == null || stored.isEmpty()) && id.length() > COUNTER_DIGITS) {
            // Older records may use the unsplit numeric ID as their member prefix.
            prefix = id + SEPARATOR;
            stored = redisTemplate.opsForZSet().rangeByLex(key,
                Range.closed(prefix, prefix + "\uffff"), Limit.limit().count(1));
        }
        return convertToMessages(key, stored).stream().filter(message -> messageId.equals(message.getId())).findFirst();
    }

    private boolean advanceRead(Integer articleId, Integer roomId, Integer userId, Long messageId) {
        String key = getChatRoomKey(articleId, roomId);
        var lock = redissonClient.getLock(key + ":save-lock");
        lock.lock();
        try {
            boolean advanced = Long.valueOf(1).equals(redisTemplate.execute(ADVANCE_READ,
                List.of(key + ":read"), userId.toString(), messageId.toString()));
            Object receipt = redisTemplate.opsForHash().get(key + ":read", userId.toString());
            long readThrough = receipt == null ? messageId : Long.parseLong(receipt.toString());
            Set<String> stored = redisTemplate.opsForZSet().range(key, 0, -1);
            if (stored != null) {
                for (String member : stored) {
                    var parsed = convertToMessages(key, Set.of(member));
                    if (parsed.isEmpty()) continue;
                    var message = parsed.get(0);
                    if (message.getId() <= readThrough && !userId.equals(message.getUserId())
                        && !Boolean.TRUE.equals(message.getIsRead())) {
                        message.readMessage();
                        String replacement = member.substring(0, member.indexOf(SEPARATOR) + 1)
                            + objectMapper.writeValueAsString(message);
                        // Replace only the observed member atomically; never delete the room's ZSET.
                        redisTemplate.execute(REPLACE_MESSAGE, List.of(key), member, replacement);
                    }
                }
            }
            return advanced;
        } catch (JsonProcessingException e) {
            throw new MessageProcessingException("메시지 읽음 저장 실패: " + key, e);
        } finally {
            lock.unlock();
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

    private List<ChatMessageEntity> convertToMessages(String roomKey, Set<String> messageJsonSet) {
        if (messageJsonSet == null || messageJsonSet.isEmpty()) {
            return Collections.emptyList();
        }

        return messageJsonSet.stream()
            .map(value -> {
                try {
                    int separator = value.indexOf(SEPARATOR);
                    if (separator < 1) {
                        throw new IllegalArgumentException("Missing message key separator");
                    }
                    String json = value.substring(separator + 1);
                    ChatMessageEntity message = objectMapper.readValue(json, ChatMessageEntity.class);
                    if (message == null || message.getId() == null || message.getId() < 1) {
                        throw new IllegalArgumentException("Missing or invalid stored message ID");
                    }
                    return message;
                } catch (JsonProcessingException | IllegalArgumentException e) {
                    return null;
                }
            })
            .filter(Objects::nonNull)
            .sorted(Comparator.comparing(ChatMessageEntity::getId))
            .toList();
    }
}
