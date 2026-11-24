package in.koreatech.koin.acceptance.domain.chat;

import static org.assertj.core.api.Assertions.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import org.junit.jupiter.api.*;
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
import in.koreatech.koin.domain.community.lostitem.chatmessage.model.ChatMessageEntity;
import in.koreatech.koin.domain.community.lostitem.chatmessage.repository.ChatMessageRedisRepository;

@Testcontainers
class LostItemChatMessageRedisTest {
    @Container
    private static final GenericContainer<?> REDIS = new GenericContainer<>(DockerImageName.parse("redis:7.0.9"))
        .withExposedPorts(6379);
    private static LettuceConnectionFactory connection;
    private static StringRedisTemplate redis;
    private static RedissonClient redisson;
    private static ChatMessageRedisRepository repository;
    private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();

    @BeforeAll
    static void setup() {
        connection = new LettuceConnectionFactory(REDIS.getHost(), REDIS.getMappedPort(6379));
        connection.afterPropertiesSet();
        redis = new StringRedisTemplate(connection);
        Config config = new Config();
        config.useSingleServer().setAddress("redis://" + REDIS.getHost() + ":" + REDIS.getMappedPort(6379));
        redisson = Redisson.create(config);
        repository = new ChatMessageRedisRepository(redis, MAPPER, redisson);
    }

    @AfterAll
    static void cleanup() {
        if (redisson != null) redisson.shutdown();
        if (connection != null) connection.destroy();
    }

    @Test
    void concurrentLegacyReadsNeverLoseSavesAndIdsRemainMonotonic() throws Exception {
        int room = 1;
        var seed = repository.save(message(room, 10));
        ExecutorService executor = Executors.newFixedThreadPool(6);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<?>> results = new ArrayList<>();
        try {
            for (int worker = 0; worker < 6; worker++) {
                int lane = worker;
                results.add(executor.submit(() -> {
                    start.await();
                    for (int i = 0; i < 25; i++) {
                        if (lane % 2 == 0) repository.save(message(room, 10));
                        else repository.markAllMessagesAsRead(1, room, 20);
                    }
                    return null;
                }));
            }
            start.countDown();
            for (var result : results) result.get(30, TimeUnit.SECONDS);
        } finally {
            start.countDown();
            executor.shutdownNow();
        }
        var stored = repository.findByArticleIdAndChatRoomId(1, room);
        assertThat(stored).hasSize(76);
        assertThat(stored).extracting(ChatMessageEntity::getId).doesNotHaveDuplicates().isSorted();
        assertThat(stored.get(0).getId()).isEqualTo(seed.getId());
        var last = stored.get(stored.size() - 1);
        repository.markRead(1, room, 20, last.getId());
        repository.markRead(1, room, 20, seed.getId());
        assertThat(repository.findByArticleIdAndChatRoomId(1, room)).allMatch(ChatMessageEntity::getIsRead);
        var next = repository.save(message(room, 10));
        assertThat(next.getId()).isGreaterThan(last.getId());
        assertThat(repository.findByArticleIdAndChatRoomId(1, room).get(76).getIsRead()).isFalse();
    }

    @Test
    void receiptPrecisionAndLegacyTrueHistoryArePreservedInRawJson() throws Exception {
        String key = "article:1:chatroom:2:message";
        // Above IEEE-754 exact integer range: adjacent ids must stay distinguishable.
        long first = 9007199254740993L;
        for (int i = 0; i < 3; i++) {
            var entity = message(2, i == 2 ? 20 : 10).toBuilder().id(first + i).isRead(i == 2).build();
            redis.opsForZSet().add(key, Long.toString(first + i) + "|" + MAPPER.writeValueAsString(entity), 0);
        }
        repository.markRead(1, 2, 20, first);
        assertThat(repository.findByArticleIdAndChatRoomId(1, 2)).extracting(ChatMessageEntity::getIsRead)
            .containsExactly(true, false, true);
        repository.markRead(1, 2, 20, first + 1);
        repository.markRead(1, 2, 20, first);
        assertThat(redis.opsForHash().get(key + ":read", "20")).isEqualTo(Long.toString(first + 1));
        assertThat(redis.opsForZSet().range(key, 0, -1)).hasSize(3).allSatisfy(member -> {
            var raw = MAPPER.readValue(member.substring(member.indexOf('|') + 1), ChatMessageEntity.class);
            assertThat(raw.getIsRead()).isTrue();
        });
    }

    @Test
    void persistedSequenceWinsOverClockAndSeedsFromLegacyMessages() throws Exception {
        long futureId = 8_000_000_000_000_000_000L;
        String key = "article:1:chatroom:3:message";
        var old = message(3, 10).toBuilder().id(futureId).build();
        redis.opsForZSet().add(key, futureId + "|" + MAPPER.writeValueAsString(old), 0);
        redis.opsForZSet().add(key, "broken|{invalid", 0);
        var first = repository.save(message(3, 10));
        var second = repository.save(message(3, 10));
        assertThat(first.getId()).isEqualTo(futureId + 1);
        assertThat(second.getId()).isEqualTo(futureId + 2);
        assertThat(redis.opsForValue().get(key + ":sequence")).isEqualTo(Long.toString(futureId + 2));
    }

    private static ChatMessageEntity message(int room, int sender) {
        return ChatMessageEntity.builder().id(1L).articleId(1).chatRoomId(room).userId(sender)
            .nickName("sender").contents("message").isRead(false).isImage(false).isDeleted(false)
            .createdAt(LocalDateTime.now()).build();
    }
}
