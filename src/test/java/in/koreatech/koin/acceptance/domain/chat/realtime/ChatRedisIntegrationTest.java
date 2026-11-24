package in.koreatech.koin.acceptance.domain.chat.realtime;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.Callable;
import java.util.stream.IntStream;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import in.koreatech.koin.domain.chat.config.ChatRedisConfig;
import in.koreatech.koin.domain.chat.dto.ChatMessageResponse;
import in.koreatech.koin.domain.chat.dto.ChatRealtimeEvent;
import in.koreatech.koin.domain.chat.event.ChatMessageStoredEvent;
import in.koreatech.koin.domain.chat.event.ChatRealtimeReceivedEvent;
import in.koreatech.koin.domain.chat.event.ChatRoomChangedEvent;
import in.koreatech.koin.domain.chat.model.ChatDomainType;
import in.koreatech.koin.domain.chat.model.ChatRoomKey;
import in.koreatech.koin.domain.chat.realtime.ChatRealtimePublisher;
import in.koreatech.koin.domain.chat.realtime.ChatRealtimeRedisListener;
import in.koreatech.koin.domain.chat.service.ChatPresenceService;
import in.koreatech.koin.domain.community.lostitem.chatmessage.repository.ChatRoomPollingSessionRedisRepository;

@Testcontainers
class ChatRedisIntegrationTest {
    @Container
    static final GenericContainer<?> REDIS = new GenericContainer<>(DockerImageName.parse("redis:7.0.9"))
        .withExposedPorts(6379);

    private LettuceConnectionFactory connection;
    private StringRedisTemplate redis;
    private ChatPresenceService presence;
    private ChatRoomPollingSessionRedisRepository polling;
    private final ChatRoomKey room = new ChatRoomKey(ChatDomainType.LOST_ITEM, 10, 2);

    @BeforeEach
    void setUp() {
        connection = new LettuceConnectionFactory(REDIS.getHost(), REDIS.getMappedPort(6379));
        connection.afterPropertiesSet();
        redis = new StringRedisTemplate(connection);
        polling = new ChatRoomPollingSessionRedisRepository(redis);
        presence = new ChatPresenceService(redis, polling, org.mockito.Mockito.mock(
            in.koreatech.koin.socket.session.service.WebSocketUserSessionService.class));
        try (var client = connection.getConnection()) {
            client.serverCommands().flushDb();
        }
    }

    @AfterEach
    void tearDown() {
        connection.destroy();
    }

    @Test
    void leave_removes_only_callers_session_and_scopes_membership_to_user_and_room() {
        presence.touch(room, 7, "ws:first");
        presence.touch(room, 7, "ws:second");
        presence.touch(room, 8, "ws:first");
        presence.leave(room, 7, "ws:first");
        assertThat(presence.isOnline(room, 7)).isTrue();
        presence.leave(room, 7, "ws:second");
        assertThat(presence.isOnline(room, 7)).isFalse();
        assertThat(presence.isOnline(room, 8)).isTrue();
        assertThat(presence.isOnline(new ChatRoomKey(ChatDomainType.CALLVAN, 10, 2), 8)).isFalse();
        presence.leave(room, 8, "ws:unknown");
        assertThat(presence.isOnline(room, 8)).isTrue();
    }

    @Test
    void legacy_and_common_polling_keep_independent_twenty_second_leases() {
        polling.markUserOnline(10, 2, 7);
        assertThat(presence.isOnline(room, 7)).isTrue();
        presence.leavePolling(room, 7);
        assertThat(polling.isUserOnline(10, 2, 7)).isTrue();
        assertThat(redis.getExpire("chat:article:10:room:2:user:7", TimeUnit.MILLISECONDS))
            .isBetween(1L, 20_000L);

        presence.touchPolling(room, 7);
        presence.touchPolling(room, 7);
        String key = "chat:polling:lost-item-10-2:user:7";
        assertThat(redis.opsForValue().get(key)).isEqualTo("online");
        assertThat(redis.getExpire(key, TimeUnit.MILLISECONDS)).isBetween(1L, 20_000L);
        assertThat(polling.isUserOnline(10, 2, 7)).isTrue();
        polling.markUserOffline(10, 2, 7);
        assertThat(polling.isUserOnline(10, 2, 7)).isFalse();
        assertThat(presence.isOnline(room, 7)).isTrue();
        presence.touchPolling(room, 7);
        assertThat(polling.isUserOnline(10, 2, 7)).isFalse();
        presence.leavePolling(room, 7);
        assertThat(presence.isOnline(room, 7)).isFalse();
    }

    @Test
    void ending_polling_preserves_other_users_rooms_and_native_socket_connections() {
        var otherRoom = new ChatRoomKey(ChatDomainType.CALLVAN, 10, 2);
        presence.touchPolling(room, 7);
        presence.touchPolling(room, 8);
        presence.touchPolling(otherRoom, 7);
        presence.touch(room, 7, "ws:connected");

        presence.leavePolling(room, 7);
        assertThat(polling.isUserOnline(room, 7)).isFalse();
        assertThat(presence.isOnline(room, 7)).isTrue();
        assertThat(presence.isOnline(room, 8)).isTrue();
        assertThat(presence.isOnline(otherRoom, 7)).isTrue();
        presence.leave(room, 7, "ws:connected");
        assertThat(presence.isOnline(room, 7)).isFalse();
    }

    @Test
    void stale_sessions_are_pruned_atomically_without_removing_live_sessions() {
        presence.touch(room, 7, "ws:stale");
        String key = "chat:presence:v1:" + room.encoded() + ":7";
        assertThat(redis.getExpire(key, TimeUnit.MILLISECONDS)).isBetween(1L, 30_000L);
        redis.opsForZSet().add(key, "ws:stale", 1);
        presence.touch(room, 7, "ws:live");
        assertThat(redis.opsForZSet().range(key, 0, -1)).containsExactly("ws:live");
        redis.opsForZSet().add(key, "ws:live", 1);
        assertThat(presence.isOnline(room, 7)).isFalse();
        assertThat(redis.hasKey(key)).isFalse();
    }

    @Test
    void concurrent_sessions_and_repeated_heartbeat_preserve_other_connections() throws Exception {
        var executor = Executors.newFixedThreadPool(4);
        try {
            List<Callable<Void>> touches = IntStream.range(0, 20).mapToObj(index -> (Callable<Void>) () -> {
                presence.touch(room, 7, "ws:session-" + index);
                return null;
            }).toList();
            for (var result : executor.invokeAll(touches)) {
                result.get();
            }
            presence.touch(room, 7, "ws:session-0");
            String key = "chat:presence:v1:" + room.encoded() + ":7";
            assertThat(redis.opsForZSet().zCard(key)).isEqualTo(20);
            List<Callable<Void>> leaves = IntStream.range(0, 19).mapToObj(index -> (Callable<Void>) () -> {
                presence.leave(room, 7, "ws:session-" + index);
                return null;
            }).toList();
            for (var result : executor.invokeAll(leaves)) {
                result.get();
            }
            assertThat(presence.isOnline(room, 7)).isTrue();
            assertThat(redis.opsForZSet().range(key, 0, -1)).containsExactly("ws:session-19");
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void redis_expiry_removes_abandoned_sessions() throws InterruptedException {
        presence.touch(room, 7, "ws:abandoned");
        redis.expire("chat:presence:v1:" + room.encoded() + ":7", Duration.ofMillis(30));
        Thread.sleep(80);
        assertThat(presence.isOnline(room, 7)).isFalse();
    }

    @Test
    void two_nodes_each_receive_one_shared_channel_event_without_echo() throws Exception {
        var mapper = JsonMapper.builder().addModule(new JavaTimeModule()).build();
        var first = new LinkedBlockingQueue<Object>();
        var second = new LinkedBlockingQueue<Object>();
        var config = new ChatRedisConfig();
        var nodeOne = config.chatRedisMessageListenerContainer(connection, new ChatRealtimeRedisListener(mapper, first::add));
        var nodeTwo = config.chatRedisMessageListenerContainer(connection, new ChatRealtimeRedisListener(mapper, second::add));
        try {
            for (RedisMessageListenerContainer node : List.of(nodeOne, nodeTwo)) {
                node.afterPropertiesSet();
                node.start();
            }
            new ChatRealtimePublisher(redis, mapper).onRoomChanged(new ChatRoomChangedEvent(room));
            var expected = new ChatRealtimeReceivedEvent(
                new ChatRealtimeEvent(ChatRealtimeEvent.Type.ROOM_CHANGED, room.encoded(), null));
            assertThat(first.poll(5, TimeUnit.SECONDS)).isEqualTo(expected);
            assertThat(second.poll(5, TimeUnit.SECONDS)).isEqualTo(expected);
            assertThat(first.poll(100, TimeUnit.MILLISECONDS)).isNull();
            assertThat(second.poll(100, TimeUnit.MILLISECONDS)).isNull();
            var message = new ChatMessageResponse("9007199254740993", 7, "sender", "원문", LocalDateTime.now(),
                false, 1, false);
            new ChatRealtimePublisher(redis, mapper).onMessageStored(new ChatMessageStoredEvent(room, message, true));
            var messageEvent = new ChatRealtimeReceivedEvent(
                new ChatRealtimeEvent(ChatRealtimeEvent.Type.MESSAGE_CREATED, room.encoded(), message), true);
            for (var node : List.of(first, second)) {
                var received = (ChatRealtimeReceivedEvent)node.poll(5, TimeUnit.SECONDS);
                assertThat(received).isEqualTo(messageEvent);
                assertThat(received.event().message().getTranslationSourceVersion())
                    .isEqualTo(message.getTranslationSourceVersion());
                assertThat(node.poll(100, TimeUnit.MILLISECONDS)).isNull();
            }
        } finally {
            nodeOne.destroy();
            nodeTwo.destroy();
        }
    }
}
