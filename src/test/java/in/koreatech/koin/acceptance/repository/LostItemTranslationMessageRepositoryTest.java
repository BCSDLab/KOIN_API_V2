package in.koreatech.koin.acceptance.repository;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.Set;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import in.koreatech.koin.domain.community.lostitem.chatmessage.exception.MessageProcessingException;
import in.koreatech.koin.domain.community.lostitem.chatmessage.model.ChatMessageEntity;
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

    @BeforeAll
    static void setUp() {
        connectionFactory = new LettuceConnectionFactory(REDIS.getHost(), REDIS.getMappedPort(6379));
        connectionFactory.afterPropertiesSet();
        redis = new StringRedisTemplate(connectionFactory);
        mapper = new ObjectMapper().findAndRegisterModules().disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        repository = new ChatMessageRedisRepository(redis, mapper);
    }

    @AfterAll
    static void tearDown() {
        if (connectionFactory != null) {
            connectionFactory.destroy();
        }
    }

    @BeforeEach
    void clearMessages() {
        redis.delete(Set.of(KEY, "article:18:chatroom:2:message", "article:17:chatroom:3:message"));
    }

    @Test
    void selectsOnlyExactIdWithinArticleAndRoomWithoutChangingReadState() {
        repository.save(message(ID, 17, 2, "원문"));
        repository.save(message(ID - 1, 17, 2, "이전"));
        repository.save(message(ID + 1, 17, 2, "다음"));
        repository.save(message(ID, 18, 2, "다른 게시글"));
        repository.save(message(ID, 17, 3, "다른 방"));
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
    }

    @Test
    void invalidJsonForOtherIdsIsNotRead() {
        repository.save(message(ID, 17, 2, "원문"));
        redis.opsForZSet().add(KEY, prefix(ID + 1) + "{invalid", 0);
        assertThat(repository.findByArticleIdAndChatRoomIdAndId(17, 2, ID).orElseThrow().getContents())
            .isEqualTo("원문");
    }

    @Test
    void invalidJsonForRequestedIdHasFixedErrorWithoutLeakingContent() {
        redis.opsForZSet().add(KEY, prefix(ID) + "{private broken contents", 0);
        assertThatThrownBy(() -> repository.findByArticleIdAndChatRoomIdAndId(17, 2, ID))
            .isInstanceOf(MessageProcessingException.class).hasMessage("메시지 조회 실패");
    }

    @Test
    void embeddedIdOrRoomMismatchIsRejected() throws Exception {
        redis.opsForZSet().add(KEY, prefix(ID) + mapper.writeValueAsString(message(ID + 1, 17, 2, "다른 ID")), 0);
        redis.opsForZSet().add(KEY, prefix(ID) + mapper.writeValueAsString(message(ID, 18, 2, "다른 게시글")), 0);
        redis.opsForZSet().add(KEY, prefix(ID) + mapper.writeValueAsString(message(ID, 17, 3, "다른 방")), 0);
        assertThat(repository.findByArticleIdAndChatRoomIdAndId(17, 2, ID)).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(longs = {-1, Long.MIN_VALUE, 0, 1, 9999})
    void shortOrNegativeIdsAreAbsent(long id) {
        assertThat(repository.findByArticleIdAndChatRoomIdAndId(17, 2, id)).isEmpty();
    }

    @Test
    void nullAndMissingIdsAreAbsent() {
        assertThat(repository.findByArticleIdAndChatRoomIdAndId(17, 2, null)).isEmpty();
        assertThat(repository.findByArticleIdAndChatRoomIdAndId(17, 2, ID)).isEmpty();
    }

    private ChatMessageEntity message(long id, int article, int room, String contents) {
        return ChatMessageEntity.builder().id(id).articleId(article).chatRoomId(room).userId(7)
            .contents(contents).nickName("익명").createdAt(LocalDateTime.of(2026, 11, 9, 9, 0))
            .isImage(false).isRead(false).isDeleted(false).build();
    }

    private String prefix(long id) {
        String text = Long.toString(id);
        return text.substring(0, text.length() - 4) + ":" + text.substring(text.length() - 4) + "|";
    }
}
