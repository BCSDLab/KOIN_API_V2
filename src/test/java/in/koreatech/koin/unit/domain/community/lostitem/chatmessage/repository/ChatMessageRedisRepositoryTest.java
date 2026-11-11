package in.koreatech.koin.unit.domain.community.lostitem.chatmessage.repository;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.ZSetOperations;

import com.fasterxml.jackson.databind.ObjectMapper;

import in.koreatech.koin.domain.community.lostitem.chatmessage.model.ChatMessageEntity;
import in.koreatech.koin.domain.community.lostitem.chatmessage.repository.ChatMessageRedisRepository;
import in.koreatech.koin.global.exception.CustomException;

class ChatMessageRedisRepositoryTest {
    private final RedisTemplate<String, String> redis = mock(RedisTemplate.class);
    private final ZSetOperations<String, String> sorted = mock(ZSetOperations.class);
    private final HashOperations<String, Object, Object> hashes = mock(HashOperations.class);
    private final ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
    private final RedissonClient redisson = mock(RedissonClient.class);
    private final ChatMessageRedisRepository repository = new ChatMessageRedisRepository(redis, mapper, redisson);

    @BeforeEach
    void setUp() {
        when(redis.opsForZSet()).thenReturn(sorted);
        lenient().when(redis.opsForHash()).thenReturn(hashes);
    }

    @Test
    void readRejectsAnIdNotStoredInThisRoom() {
        when(sorted.rangeByLex(eq("article:1:chatroom:2:message"), any(Range.class), any())).thenReturn(Set.of());
        assertThatThrownBy(() -> repository.markRead(1, 2, 10, 123L)).isInstanceOf(CustomException.class);
        verify(sorted, never()).reverseRange(anyString(), anyLong(), anyLong());
        verifyNoInteractions(hashes);
    }
    @Test
    void corruptJsonIsSkippedWhileValidHistoryRemains() throws Exception {
        var valid = ChatMessageEntity.builder().id(9007199254740993L).build();
        when(sorted.reverseRange("article:1:chatroom:2:message", 0, -1)).thenReturn(Set.of(
            "900719925474:0993|" + mapper.writeValueAsString(valid), "900719925474:0994|{broken"));
        assertThat(repository.findByArticleIdAndChatRoomId(1, 2))
            .extracting(ChatMessageEntity::getId).containsExactly(valid.getId());
    }

    @ParameterizedTest
    @ValueSource(strings = {"{}", "123|null", "123|{}", "123|{\"id\":0}"})
    void invalidStoredEnvelopeOrIdIsSkipped(String stored) {
        when(sorted.reverseRange("article:1:chatroom:2:message", 0, -1)).thenReturn(Set.of(stored));
        assertThat(repository.findByArticleIdAndChatRoomId(1, 2)).isEmpty();
    }

    @Test
    void corruptHistoryDoesNotPreventSaveAndAlwaysReleasesLock() {
        ValueOperations<String, String> values = mock(ValueOperations.class);
        RLock lock = mock(RLock.class);
        when(redis.opsForValue()).thenReturn(values);
        when(redisson.getLock("article:1:chatroom:2:message:save-lock")).thenReturn(lock);
        when(sorted.reverseRange("article:1:chatroom:2:message", 0, -1))
            .thenReturn(Set.of("900719925474:0994|{broken"));
        assertThat(repository.save(ChatMessageEntity.builder().articleId(1).chatRoomId(2).build()).getId())
            .isPositive();
        verify(values).set(anyString(), anyString());
        verify(sorted).add(anyString(), anyString(), anyDouble());
        verify(lock).unlock();
    }

}
