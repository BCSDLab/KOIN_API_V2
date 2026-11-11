package in.koreatech.koin.acceptance.repository;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.spy;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import in.koreatech.koin.domain.community.lostitem.chatmessage.model.ChatMessageEntity;
import in.koreatech.koin.domain.community.lostitem.chatmessage.exception.MessageProcessingException;
import in.koreatech.koin.domain.community.lostitem.chatmessage.repository.ChatMessageRedisRepository;

@Testcontainers
class LostItemTranslationMessageRepositoryTest {
    private static final long ID = 733900000000012345L;
    private static final String KEY = "article:17:chatroom:2:message";
    @Container
    private static final GenericContainer<?> REDIS = new GenericContainer<>(DockerImageName.parse("redis:7.0.9"))
        .withExposedPorts(6379);
    private static LettuceConnectionFactory connectionFactory;
    private static StringRedisTemplate redis;
    private static ObjectMapper mapper;
    private static ChatMessageRedisRepository repository;
    private static RedissonClient redisson;

    @BeforeAll
    static void setUp() {
        connectionFactory = new LettuceConnectionFactory(REDIS.getHost(), REDIS.getMappedPort(6379));
        connectionFactory.afterPropertiesSet();
        redis = new StringRedisTemplate(connectionFactory);
        mapper = new ObjectMapper().findAndRegisterModules().disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        Config config = new Config();
        config.useSingleServer().setAddress("redis://" + REDIS.getHost() + ":" + REDIS.getMappedPort(6379));
        redisson = Redisson.create(config);
        repository = new ChatMessageRedisRepository(redis, mapper, redisson);
    }

    @AfterAll
    static void tearDown() {
        if (redisson != null) {
            redisson.shutdown();
        }
        if (connectionFactory != null) {
            connectionFactory.destroy();
        }
    }

    @BeforeEach
    void clearMessages() {
        redis.delete(Set.of(KEY, "article:18:chatroom:2:message", "article:17:chatroom:3:message"));
    }

    @Test
    void selectsExactMessageAndPreservesConcurrentChangesWhenMarkingRead() throws Exception {
        storeMessage(message(ID, 17, 2, "원문"));
        storeMessage(message(ID - 1, 17, 2, "이전"));
        storeMessage(message(ID + 1, 17, 2, "다음"));
        storeMessage(message(ID, 18, 2, "다른 게시글"));
        storeMessage(message(ID, 17, 3, "다른 방"));
        Set<String> before = redis.opsForZSet().range(KEY, 0, -1);

        var result = repository.findByArticleIdAndChatRoomIdAndId(17, 2, ID).orElseThrow();
        assertThat(result.getContents()).isEqualTo("원문");
        assertThat(result.getIsRead()).isFalse();
        assertThat(repository.findByArticleIdAndChatRoomIdAndId(18, 2, ID).orElseThrow().getContents())
            .isEqualTo("다른 게시글");
        assertThat(repository.findByArticleIdAndChatRoomIdAndId(17, 3, ID).orElseThrow().getContents())
            .isEqualTo("다른 방");
        assertThat(repository.findByArticleIdAndChatRoomIdAndId(17, 2, ID + 2)).isEmpty();
        assertThat(redis.opsForZSet().range(KEY, 0, -1)).isEqualTo(before);

        redis.expire(KEY, Duration.ofMinutes(1));
        ObjectMapper racingMapper = spy(mapper);
        AtomicBoolean raced = new AtomicBoolean();
        doAnswer(invocation -> {
            if (raced.compareAndSet(false, true)) {
                // 읽음 처리의 스냅샷 조회와 저장 사이에 발생한 전송·삭제·수정을 재현한다.
                storeMessage(message(ID + 2, 17, 2, "동시에 도착한 메시지"));
                for (String member : before) {
                    if (!member.contains("\"id\":" + ID + ",")) {
                        redis.opsForZSet().remove(KEY, member);
                    }
                }
                storeMessage(message(ID + 1, 17, 2, "변경된 메시지"));
            }
            return invocation.callRealMethod();
        }).when(racingMapper).writeValueAsString(any(ChatMessageEntity.class));
        new ChatMessageRedisRepository(redis, racingMapper, redisson).markAllMessagesAsRead(17, 2, 8);

        var read = repository.findByArticleIdAndChatRoomIdAndId(17, 2, ID).orElseThrow();
        assertThat(read.getId()).isEqualTo(ID);
        assertThat(read.getIsRead()).isTrue();
        assertThat(repository.findByArticleIdAndChatRoomIdAndId(17, 2, ID - 1)).isEmpty();
        var replaced = repository.findByArticleIdAndChatRoomIdAndId(17, 2, ID + 1).orElseThrow();
        assertThat(replaced.getContents()).isEqualTo("변경된 메시지");
        assertThat(replaced.getIsRead()).isFalse();
        assertThat(repository.findByArticleIdAndChatRoomIdAndId(17, 2, ID + 2).orElseThrow().getIsRead()).isFalse();
        assertThat(redis.opsForZSet().zCard(KEY)).isEqualTo(3);
        assertThat(redis.getExpire(KEY)).isPositive().isLessThanOrEqualTo(60);
        assertThat(redis.opsForZSet().rangeWithScores(KEY, 0, -1)).allSatisfy(member ->
            assertThat(member.getScore()).isZero());

        String singleMessageKey = "article:17:chatroom:3:message";
        redis.expire(singleMessageKey, Duration.ofMinutes(1));
        repository.markAllMessagesAsRead(17, 3, 8);
        assertThat(redis.getExpire(singleMessageKey)).isPositive().isLessThanOrEqualTo(60);

        String brokenKey = "article:18:chatroom:2:message";
        String member = redis.opsForZSet().range(brokenKey, 0, -1).iterator().next();
        redis.opsForZSet().remove(brokenKey, member);
        redis.opsForZSet().add(brokenKey, member.substring(0, member.indexOf('|') + 1) + "{invalid", 0);
        assertThatThrownBy(() -> repository.findByArticleIdAndChatRoomIdAndId(18, 2, ID))
            .isInstanceOf(MessageProcessingException.class).hasCauseInstanceOf(com.fasterxml.jackson.core.JsonProcessingException.class);
    }

    private void storeMessage(ChatMessageEntity message) throws Exception {
        // 기존 저장 형식의 ID와 읽음 처리 도중 외부 변경을 그대로 재현한다.
        String id = message.getId().toString();
        String prefix = id.substring(0, id.length() - 4) + ":" + id.substring(id.length() - 4);
        String key = "article:" + message.getArticleId() + ":chatroom:" + message.getChatRoomId() + ":message";
        redis.opsForZSet().add(key, prefix + "|" + mapper.writeValueAsString(message), 0);
    }

    private ChatMessageEntity message(long id, int article, int room, String contents) {
        return ChatMessageEntity.builder().id(id).articleId(article).chatRoomId(room).userId(7)
            .contents(contents).nickName("익명").createdAt(LocalDateTime.of(2026, 11, 9, 9, 0))
            .isImage(false).isRead(false).isDeleted(false).build();
    }

}
