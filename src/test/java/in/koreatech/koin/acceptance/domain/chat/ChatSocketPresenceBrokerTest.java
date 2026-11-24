package in.koreatech.koin.acceptance.domain.chat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.broker.SimpleBrokerMessageHandler;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.messaging.support.AbstractSubscribableChannel;
import org.springframework.messaging.support.ExecutorChannelInterceptor;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

import com.fasterxml.jackson.databind.ObjectMapper;

import in.koreatech.koin.domain.chat.model.ChatRoomKey;
import in.koreatech.koin.domain.chat.service.ChatPresenceService;
import in.koreatech.koin.domain.chat.service.ChatService;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.repository.UserRepository;
import in.koreatech.koin.domain.user.web.service.WebAuthService;
import in.koreatech.koin.global.auth.JwtProvider;
import in.koreatech.koin.global.auth.WebCsrfTokenProvider;
import in.koreatech.koin.global.code.ApiResponseCode;
import in.koreatech.koin.global.exception.CustomException;
import in.koreatech.koin.socket.config.WebSocketConfig;
import in.koreatech.koin.socket.interceptor.ChatHandshakeInterceptor;
import in.koreatech.koin.socket.interceptor.ChatOutboundInterceptor;
import in.koreatech.koin.socket.interceptor.ConnectInterceptor;
import in.koreatech.koin.socket.session.service.ChatSocketSessionRegistry;

/** Real Spring broker wiring and Redis leases; authentication and membership lookups are isolated mocks. */
@SpringJUnitWebConfig(ChatSocketPresenceBrokerTest.Config.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class ChatSocketPresenceBrokerTest {
    private static final String LIST = "/topic/chatroom/list/7";
    private static final String LEGACY_KEY = "SocketSessionUserId:7";
    private static final String ROOM = "lost-item-1-2";
    private static final String OTHER_ROOM = "lost-item-3-4";
    private static final String TOPIC = "/topic/chatrooms/" + ROOM;
    private static final String OTHER_TOPIC = "/topic/chatrooms/" + OTHER_ROOM;

    @Autowired ChatSocketSessionRegistry sessions;
    @Autowired ChatService chat;
    @Autowired JwtProvider jwt;
    @Autowired UserRepository users;
    @Autowired StringRedisTemplate redis;
    @Autowired SimpleBrokerMessageHandler broker;
    @Autowired ConnectInterceptor inbound;
    @Autowired @Qualifier("clientInboundChannel") AbstractSubscribableChannel inboundChannel;
    @Autowired ChatPresenceService presence;
    @Autowired in.koreatech.koin.socket.session.service.WebSocketUserSessionService legacySessions;
    @Autowired in.koreatech.koin.domain.community.lostitem.chatroom.service.LostItemChatAccessService legacyAccess;

    @BeforeEach
    void setUp() {
        reset(chat, jwt, users, legacyAccess);
        when(jwt.getUserId("mobile")).thenReturn(7);
        User user = mock(User.class);
        when(user.getId()).thenReturn(7);
        when(users.getById(7)).thenReturn(user);
        when(chat.validateReadAccess(7, ROOM)).thenReturn(ChatRoomKey.parse(ROOM));
        when(chat.validateReadAccess(7, OTHER_ROOM)).thenReturn(ChatRoomKey.parse(OTHER_ROOM));
        redis.delete(LEGACY_KEY);
        connect("device-a");
        connect("device-b");
    }

    @AfterEach
    void cleanUp() {
        for (String id : List.of("device-a", "device-b")) {
            sessions.disconnect(id);
            broker.getSubscriptionRegistry().unregisterAllSubscriptions(id);
        }
        redis.delete(LEGACY_KEY);
    }

    @Test
    void legacyRoomUsesWeekLongRedisStateWithoutHeartbeatOrTokenReauthentication() {
        subscribe("device-a", "legacy", "/topic/chat/1/2");
        var initial = legacySessions.read(7).orElseThrow();
        assertThat(initial.getStatus()).isEqualTo(
            in.koreatech.koin.socket.session.model.WebSocketUserSessionStatus.ACTIVE_CHAT_ROOM);
        assertThat(initial.getCurrentArticleId()).isEqualTo(1L);
        assertThat(initial.getCurrentChatRoomId()).isEqualTo(2L);
        assertThat(redis.getExpire(LEGACY_KEY, TimeUnit.SECONDS)).isBetween(604_790L, 604_800L);
        assertThat(redis.hasKey("chat:presence:v1:" + ROOM + ":7")).isFalse();
        assertThat(redis.hasKey("chat:presence:legacy-room:v1:" + ROOM + ":7")).isFalse();
        when(jwt.getUserId("mobile")).thenThrow(
            in.koreatech.koin.global.auth.exception.AuthenticationException.withDetail("expired"));
        clearInvocations(jwt);

        // Delivery and send remain valid without any heartbeat after token expiry.
        assertThat(sessions.canDeliver("device-a", "legacy", "/topic/chat/1/2")).isTrue();
        sessions.validateSend("device-a", "/app/chat/1/2");
        sessions.heartbeat("device-a");

        verifyNoInteractions(jwt);
        verify(legacyAccess, times(2)).validateParticipant(1, 2, 7);
        assertThat(legacySessions.read(7).orElseThrow().getLastActiveAt()).isEqualTo(initial.getLastActiveAt());
        clientUnsubscribe("device-a", "legacy");
        assertThat(legacySessions.read(7).orElseThrow().getStatus()).isEqualTo(
            in.koreatech.koin.socket.session.model.WebSocketUserSessionStatus.ACTIVE_APP);
        sessions.disconnect("device-a");
        assertThat(legacySessions.read(7)).isEmpty();
    }

    @Test
    void legacyListAndRoomSharePerUserStateAndDisconnectKeepsOtherLocalDevice() {
        subscribe("device-a", "list", LIST);
        subscribe("device-b", "room", "/topic/chat/1/2");
        assertThat(legacySessions.read(7).orElseThrow().getStatus()).isEqualTo(
            in.koreatech.koin.socket.session.model.WebSocketUserSessionStatus.ACTIVE_CHAT_ROOM);

        sessions.disconnect("device-b");

        assertThat(legacySessions.read(7).orElseThrow().getStatus()).isEqualTo(
            in.koreatech.koin.socket.session.model.WebSocketUserSessionStatus.ACTIVE_CHAT_ROOM_LIST);
        assertThat(sessions.canDeliver("device-a", "list", LIST)).isTrue();
        assertThat(redis.hasKey("chat:presence:list:v1:7")).isFalse();
        sessions.disconnect("device-a");
        assertThat(legacySessions.read(7)).isEmpty();
    }

    @Test
    void commonRoomStillUsesExpiringPresenceRenewedByHeartbeat() {
        subscribe("device-a", "common", TOPIC);
        String key = "chat:presence:v1:" + ROOM + ":7";
        assertThat(presence.isOnline(ChatRoomKey.parse(ROOM), 7)).isTrue();
        assertThat(redis.getExpire(key, TimeUnit.MILLISECONDS)).isBetween(1L, 30_000L);
        redis.opsForZSet().add(key, "ws:device-a", 1);
        assertThat(presence.isOnline(ChatRoomKey.parse(ROOM), 7)).isFalse();

        sessions.heartbeat("device-a");

        assertThat(presence.isOnline(ChatRoomKey.parse(ROOM), 7)).isTrue();
        assertThat(legacySessions.read(7)).isEmpty();
        sessions.disconnect("device-a");
        assertThat(presence.isOnline(ChatRoomKey.parse(ROOM), 7)).isFalse();
    }

    @Test
    void revokedSubscriptionIdCanBeReusedForAnotherDestinationInActualBroker() {
        subscribe("device-a", "reused", TOPIC);
        denyRoom();
        sessions.heartbeat("device-a");

        subscribe("device-a", "reused", OTHER_TOPIC);

        assertThat(broker.getSubscriptionRegistry().findSubscriptions(delivery(TOPIC)))
            .doesNotContainKey("device-a");
        assertThat(broker.getSubscriptionRegistry().findSubscriptions(delivery(OTHER_TOPIC)).get("device-a"))
            .containsExactly("reused");
        assertThat(sessions.canDeliver("device-a", "reused", OTHER_TOPIC)).isTrue();
    }

    @Test
    void heartbeatRevocationRemovesRealBrokerRegistrationAndKeepsOtherRoomAndDevice() {
        subscribeRooms();
        denyRoom();

        sessions.heartbeat("device-a");

        assertOnlyOwnRevokedRoomRemoved();
    }

    @Test
    void outboundRevocationRemovesRealBrokerRegistrationAndKeepsOtherRoomAndDevice() {
        subscribeRooms();
        denyRoom();

        assertThat(sessions.canDeliver("device-a", "revoked", TOPIC)).isFalse();

        assertOnlyOwnRevokedRoomRemoved();
    }

    @Test
    void clientUnsubscribeStillPassesThroughNormalInboundAndBrokerPath() {
        subscribeRooms();

        clientUnsubscribe("device-a", "revoked");

        assertOnlyOwnRevokedRoomRemoved();
    }

    @Test
    void revokedPendingSubscribeCannotRegisterAfterHeartbeat() throws Exception {
        var message = subscribeMessage("device-a", "pending", TOPIC);
        try (var gate = new BrokerGate(message)) {
            inboundChannel.send(message);
            gate.awaitEntered();
            denyRoom();
            sessions.heartbeat("device-a");
            gate.finish();
            assertThat(broker.getSubscriptionRegistry().findSubscriptions(delivery(TOPIC)))
                .doesNotContainKey("device-a");
        }
    }

    @Test
    void brokerDisconnectIsTerminalForPendingSubscribe() throws Exception {
        var message = subscribeMessage("device-a", "pending", TOPIC);
        try (var gate = new BrokerGate(message)) {
            inboundChannel.send(message);
            gate.awaitEntered();
            broker.getSubscriptionRegistry().unregisterAllSubscriptions("device-a");
            gate.finish();
            assertThat(broker.getSubscriptionRegistry().findSubscriptions(delivery(TOPIC)))
                .doesNotContainKey("device-a");
            assertThatThrownBy(() -> sessions.authenticate("device-a"))
                .isInstanceOf(in.koreatech.koin.global.auth.exception.AuthenticationException.class);
        }
    }

    @Test
    void delayedUnsubscribeCannotRemoveReplacementWithSameId() throws Exception {
        subscribe("device-a", "reused", TOPIC);
        var message = unsubscribeMessage("device-a", "reused");
        try (var gate = new BrokerGate(message)) {
            inboundChannel.send(message);
            gate.awaitEntered();
            subscribe("device-a", "reused", OTHER_TOPIC);
            gate.finish();
            assertThat(broker.getSubscriptionRegistry().findSubscriptions(delivery(OTHER_TOPIC)).get("device-a"))
                .containsExactly("reused");
            assertThat(broker.getSubscriptionRegistry().findSubscriptions(delivery(TOPIC)))
                .doesNotContainKey("device-a");
        }
    }

    @Test
    void staleDeliveryFailureCannotRevokeReplacementSubscription() throws Exception {
        subscribe("device-a", "reused", TOPIC);
        CountDownLatch failureChecked = new CountDownLatch(1);
        CountDownLatch continueFailure = new CountDownLatch(1);
        CustomException failure = mock(CustomException.class);
        when(failure.getErrorCode()).thenAnswer(invocation -> {
            failureChecked.countDown();
            assertThat(continueFailure.await(5, TimeUnit.SECONDS)).isTrue();
            return ApiResponseCode.TEAM_RECRUITMENT_CHAT_FORBIDDEN;
        });
        when(chat.validateReadAccess(7, ROOM)).thenThrow(failure);
        var delivery = CompletableFuture.supplyAsync(() -> sessions.canDeliver("device-a", "reused", TOPIC));
        assertThat(failureChecked.await(5, TimeUnit.SECONDS)).isTrue();
        var replacement = CompletableFuture.runAsync(() -> {
            clientUnsubscribe("device-a", "reused");
            subscribe("device-a", "reused", OTHER_TOPIC);
        });
        try {
            // A correct implementation may hold the connection lock until failure cleanup is complete.
            replacement.get(200, TimeUnit.MILLISECONDS);
        } catch (TimeoutException expected) {
            // Let cleanup finish before waiting for a replacement serialized behind it.
        } finally {
            continueFailure.countDown();
        }
        assertThat(delivery.get(5, TimeUnit.SECONDS)).isFalse();
        replacement.get(5, TimeUnit.SECONDS);
        assertThat(sessions.canDeliver("device-a", "reused", OTHER_TOPIC)).isTrue();
        assertThat(broker.getSubscriptionRegistry().findSubscriptions(delivery(OTHER_TOPIC)).get("device-a"))
            .containsExactly("reused");
    }

    private final class BrokerGate implements ExecutorChannelInterceptor, AutoCloseable {
        private final Message<?> target;
        private final CountDownLatch entered = new CountDownLatch(1);
        private final CountDownLatch release = new CountDownLatch(1);
        private final CountDownLatch handled = new CountDownLatch(1);

        private BrokerGate(Message<?> target) {
            this.target = target;
            inboundChannel.addInterceptor(this);
        }

        @Override
        public Message<?> beforeHandle(Message<?> message, MessageChannel channel, MessageHandler handler) {
            if (message == target && handler == broker) {
                entered.countDown();
                try {
                    assertThat(release.await(5, TimeUnit.SECONDS)).isTrue();
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException(exception);
                }
            }
            return message;
        }

        @Override
        public void afterMessageHandled(Message<?> message, MessageChannel channel, MessageHandler handler,
            Exception exception) {
            if (message == target && handler == broker) {
                handled.countDown();
            }
        }

        private void awaitEntered() throws InterruptedException {
            assertThat(entered.await(5, TimeUnit.SECONDS)).isTrue();
        }

        private void finish() throws InterruptedException {
            release.countDown();
            assertThat(handled.await(5, TimeUnit.SECONDS)).isTrue();
        }

        @Override
        public void close() {
            release.countDown();
            inboundChannel.removeInterceptor(this);
        }
    }

    private void subscribeRooms() {
        subscribe("device-a", "revoked", TOPIC);
        subscribe("device-a", "retained", OTHER_TOPIC);
        subscribe("device-b", "revoked", TOPIC);
        assertThat(broker.getSubscriptionRegistry().findSubscriptions(delivery(TOPIC)).get("device-a"))
            .containsExactly("revoked");
    }

    private void denyRoom() {
        when(chat.validateReadAccess(7, ROOM))
            .thenThrow(CustomException.of(ApiResponseCode.TEAM_RECRUITMENT_CHAT_FORBIDDEN));
    }

    private void assertOnlyOwnRevokedRoomRemoved() {
        var revoked = broker.getSubscriptionRegistry().findSubscriptions(delivery(TOPIC));
        assertThat(revoked).doesNotContainKey("device-a");
        assertThat(revoked.get("device-b")).containsExactly("revoked");
        assertThat(broker.getSubscriptionRegistry().findSubscriptions(delivery(OTHER_TOPIC)).get("device-a"))
            .containsExactly("retained");
        assertThat(sessions.canDeliver("device-a", "retained", OTHER_TOPIC)).isTrue();
        assertThat(sessions.authenticate("device-a").getUserId()).isEqualTo(7);
    }

    private void connect(String session) {
        var headers = StompHeaderAccessor.create(StompCommand.CONNECT);
        headers.setSessionId(session);
        headers.setNativeHeader("Authorization", "mobile");
        sessions.connect(headers);
    }

    private void subscribe(String session, String id, String destination) {
        var message = subscribeMessage(session, id, destination);
        assertThat(inbound.preSend(message, null)).isSameAs(message);
        broker.handleMessage(message);
    }

    private Message<byte[]> subscribeMessage(String session, String id, String destination) {
        var headers = StompHeaderAccessor.create(StompCommand.SUBSCRIBE);
        headers.setSessionId(session);
        headers.setSubscriptionId(id);
        headers.setDestination(destination);
        headers.setLeaveMutable(true);
        return MessageBuilder.createMessage(new byte[0], headers.getMessageHeaders());
    }

    private void clientUnsubscribe(String session, String id) {
        var message = unsubscribeMessage(session, id);
        assertThat(inbound.preSend(message, null)).isSameAs(message);
        broker.handleMessage(message);
    }

    private Message<byte[]> unsubscribeMessage(String session, String id) {
        var headers = StompHeaderAccessor.create(StompCommand.UNSUBSCRIBE);
        headers.setSessionId(session);
        headers.setSubscriptionId(id);
        headers.setLeaveMutable(true);
        return MessageBuilder.createMessage(new byte[0], headers.getMessageHeaders());
    }

    private Message<byte[]> delivery(String destination) {
        var headers = SimpMessageHeaderAccessor.create(SimpMessageType.MESSAGE);
        headers.setDestination(destination);
        return MessageBuilder.createMessage(new byte[0], headers.getMessageHeaders());
    }

    @Configuration(proxyBeanMethods = false)
    @Import({WebSocketConfig.class, ChatSocketSessionRegistry.class, ConnectInterceptor.class,
        ChatOutboundInterceptor.class, ChatPresenceService.class,
        in.koreatech.koin.socket.session.service.WebSocketUserSessionService.class,
        in.koreatech.koin.socket.session.repository.WebSocketUserSessionRedisRepository.class,
        in.koreatech.koin.domain.community.lostitem.chatmessage.repository.ChatRoomPollingSessionRedisRepository.class})
    static class Config {
        @Bean(initMethod = "start", destroyMethod = "stop")
        GenericContainer<?> redisContainer() {
            return new GenericContainer<>(DockerImageName.parse("redis:7.0.9")).withExposedPorts(6379);
        }
        @Bean LettuceConnectionFactory redisConnectionFactory(GenericContainer<?> redisContainer) {
            return new LettuceConnectionFactory(redisContainer.getHost(), redisContainer.getMappedPort(6379));
        }
        @Bean StringRedisTemplate redisTemplate(LettuceConnectionFactory connection) {
            return new StringRedisTemplate(connection);
        }
        @Bean org.springframework.data.redis.core.RedisTemplate<String, Object> legacyRedisTemplate(
            LettuceConnectionFactory connection) {
            var template = new org.springframework.data.redis.core.RedisTemplate<String, Object>();
            template.setConnectionFactory(connection);
            template.setKeySerializer(new org.springframework.data.redis.serializer.StringRedisSerializer());
            template.setValueSerializer(new org.springframework.data.redis.serializer.StringRedisSerializer());
            return template;
        }
        @Bean ObjectMapper objectMapper() {
            return new ObjectMapper().registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        }
        @Bean in.koreatech.koin.domain.community.lostitem.chatroom.service.LostItemChatAccessService legacyAccess() {
            return mock(in.koreatech.koin.domain.community.lostitem.chatroom.service.LostItemChatAccessService.class);
        }
        @Bean ChatService chatService() { return mock(ChatService.class); }
        @Bean JwtProvider jwtProvider() { return mock(JwtProvider.class); }
        @Bean UserRepository userRepository() { return mock(UserRepository.class); }
        @Bean WebAuthService webAuthService() { return mock(WebAuthService.class); }
        @Bean WebCsrfTokenProvider csrfTokenProvider() { return mock(WebCsrfTokenProvider.class); }
        @Bean ChatHandshakeInterceptor handshakeInterceptor() { return mock(ChatHandshakeInterceptor.class); }
    }
}
