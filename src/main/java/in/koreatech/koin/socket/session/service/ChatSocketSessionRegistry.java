package in.koreatech.koin.socket.session.service;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.context.event.EventListener;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import in.koreatech.koin.domain.chat.model.ChatRoomKey;
import in.koreatech.koin.domain.chat.service.ChatPresenceService;
import in.koreatech.koin.domain.chat.service.ChatService;
import in.koreatech.koin.domain.chat.model.ChatDomainType;
import in.koreatech.koin.domain.community.lostitem.chatroom.service.LostItemChatAccessService;
import in.koreatech.koin.socket.session.model.WebSocketUserSession;
import in.koreatech.koin.socket.session.model.WebSocketUserSessionStatus;
import in.koreatech.koin.domain.user.repository.UserRepository;
import in.koreatech.koin.domain.user.web.service.WebAuthService;
import in.koreatech.koin.global.auth.JwtProvider;
import in.koreatech.koin.global.auth.WebCsrfTokenProvider;
import in.koreatech.koin.global.auth.exception.AuthenticationException;
import in.koreatech.koin.global.exception.CustomException;
import in.koreatech.koin.socket.config.auth.UserPrincipal;
import in.koreatech.koin.socket.interceptor.ChatHandshakeInterceptor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatSocketSessionRegistry {

    public static final String SUBSCRIPTION_GENERATION = "chatSubscriptionGeneration";
    private static final Pattern LEGACY_ROOM = Pattern.compile("/topic/chat/([1-9][0-9]*)/([1-9][0-9]*)");
    private static final Pattern LEGACY_SEND = Pattern.compile("/app/chat/([1-9][0-9]*)/([1-9][0-9]*)");
    private static final Pattern COMMON_SEND = Pattern.compile("/app/chatrooms/([^/]+)/(messages|read)");

    private final JwtProvider jwtProvider;
    private final WebAuthService webAuthService;
    private final WebCsrfTokenProvider csrfTokenProvider;
    private final UserRepository userRepository;
    private final ChatService chatService;
    private final ChatPresenceService presenceService;
    private final ApplicationEventPublisher events;
    private final WebSocketUserSessionService legacySessions;
    private final LostItemChatAccessService legacyAccess;
    // 인증 토큰은 프로세스 내부에서만 보관하고 공유 접속 상태나 메시지 헤더에 기록하지 않는다.
    private final Map<String, Connection> connections = new ConcurrentHashMap<>();
    private final Map<String, WebSocketSession> transports = new ConcurrentHashMap<>();
    private final AtomicLong subscriptionSequence = new AtomicLong();
    private final Object[] legacyStateLocks = java.util.stream.IntStream.range(0, 64)
        .mapToObj(index -> new Object()).toArray();

    public synchronized void transportEstablished(WebSocketSession session) {
        transports.put(session.getId(), session);
    }

    public void transportClosed(String sessionId) {
        transports.remove(sessionId);
        disconnect(sessionId);
    }

    private void revoke(String sessionId) {
        WebSocketSession transport = transports.remove(sessionId);
        disconnect(sessionId);
        if (transport != null) {
            try {
                transport.close(CloseStatus.POLICY_VIOLATION);
            } catch (IOException | RuntimeException exception) {
                log.warn("Chat socket transport close failed: session={}, error={}", sessionId,
                    exception.getClass().getSimpleName());
            }
        }
    }

    public synchronized UserPrincipal connect(StompHeaderAccessor headers) {
        String sessionId = headers.getSessionId();
        if (sessionId == null || connections.containsKey(sessionId)) {
            throw denied();
        }
        String authorization = headers.getFirstNativeHeader("Authorization");
        boolean cookie = authorization == null;
        String token;
        Integer userId;
        if (cookie) {
            Object context = headers.getSessionAttributes() == null ? null
                : headers.getSessionAttributes().get(ChatHandshakeInterceptor.AUTH_CONTEXT);
            if (!(context instanceof ChatHandshakeInterceptor.CookieContext captured)) {
                throw denied();
            }
            if (!captured.trustedOrigin()) {
                throw denied();
            }
            token = captured.accessToken();
            var session = webAuthService.authenticate(token);
            csrfTokenProvider.validate(session, headers.getFirstNativeHeader("X-CSRF-Token"));
            userId = session.userId();
        } else {
            token = authorization.startsWith("Bearer ") ? authorization.substring(7) : authorization;
            userId = jwtProvider.getUserId(token);
        }
        UserPrincipal principal = UserPrincipal.of(userRepository.getById(userId));
        connections.put(sessionId, new Connection(token, cookie, principal));
        return principal;
    }

    public UserPrincipal authenticate(String sessionId) {
        Connection connection = connection(sessionId);
        try {
            Integer userId = connection.cookie ? webAuthService.authenticate(connection.token).userId()
                : jwtProvider.getUserId(connection.token);
            if (!connection.principal.getUserId().equals(userId)) {
                throw denied();
            }
            userRepository.getById(userId);
            return connection.principal;
        } catch (RuntimeException exception) {
            if (isAccessLost(exception)) {
                if (connection.subscriptions.values().stream()
                    .anyMatch(subscription -> isLegacyDestination(subscription.destination))) {
                    Map.copyOf(connection.subscriptions).forEach((id, subscription) -> {
                        if (!isLegacyDestination(subscription.destination)) {
                            revokeSubscription(sessionId, id, subscription.generation);
                        }
                    });
                } else {
                    revoke(sessionId);
                }
            }
            log.warn("Chat socket authentication failed: session={}, error={}", sessionId,
                exception.getClass().getSimpleName());
            throw exception;
        }
    }

    public void validateSend(String sessionId, String destination) {
        String value = destination == null ? "" : destination;
        Matcher legacy = LEGACY_SEND.matcher(value);
        if (legacy.matches()) {
            Connection connection = connection(sessionId);
            legacyAccess.validateParticipant(Integer.valueOf(legacy.group(1)), Integer.valueOf(legacy.group(2)),
                connection.principal.getUserId());
            connection.usedLegacy = true;
            return;
        }
        Matcher common = COMMON_SEND.matcher(value);
        if (!common.matches()) {
            throw denied();
        }
        chatService.validateReadAccess(authenticate(sessionId).getUserId(), common.group(1));
    }

    public long subscribe(String sessionId, String subscriptionId, String destination) {
        Connection connection = connection(sessionId);
        boolean legacy = isLegacyDestination(destination);
        UserPrincipal principal = legacy ? connection.principal : authenticate(sessionId);
        synchronized (connection) {
            if (subscriptionId == null || subscriptionId.isBlank() || connection.closed
                || connection.subscriptions.containsKey(subscriptionId)) {
                throw denied();
            }
            ChatRoomKey room = validateDestination(principal.getUserId(), destination);
            if (room != null && !legacy) {
                touchRoomPresence(room, principal.getUserId(), sessionId);
            }
            long generation = subscriptionSequence.incrementAndGet();
            connection.subscriptions.put(subscriptionId, new Subscription(destination, room, generation));
            if (legacy) {
                connection.usedLegacy = true;
                refreshLegacyStatus(principal.getUserId());
            }
            return generation;
        }
    }

    public Long unsubscribe(String sessionId, String subscriptionId) {
        Connection connection = connections.get(sessionId);
        if (connection == null) {
            return null;
        }
        synchronized (connection) {
            Subscription removed = connection.subscriptions.remove(subscriptionId);
            if (removed == null) {
                return null;
            }
            if (isLegacyDestination(removed.destination)) {
                refreshLegacyStatus(connection.principal.getUserId());
            } else if (removed.room != null && connection.subscriptions.values().stream()
                .noneMatch(subscription -> removed.room.equals(subscription.room)
                    && !isLegacyDestination(subscription.destination))) {
                leavePresence(removed.room, connection.principal.getUserId(), sessionId);
            }
            return removed.generation;
        }
    }

    private void revokeSubscription(String sessionId, String subscriptionId, long generation) {
        Connection connection = connections.get(sessionId);
        if (connection == null) {
            return;
        }
        synchronized (connection) {
            Subscription current = connection.subscriptions.get(subscriptionId);
            if (current == null || current.generation != generation) {
                return;
            }
            unsubscribe(sessionId, subscriptionId);
            // 서버가 취소한 구독은 broker에서도 해제해야 같은 ID로 다시 구독할 수 있다.
            events.publishEvent(new SubscriptionRevoked(sessionId, subscriptionId, generation));
        }
    }

    public boolean canDeliver(String sessionId, String subscriptionId, String destination) {
        try {
            Connection connection = connection(sessionId);
            boolean legacy = isLegacyDestination(destination);
            UserPrincipal principal = legacy ? connection.principal : authenticate(sessionId);
            synchronized (connection) {
                Subscription subscription = connection.subscriptions.get(subscriptionId);
                if (connection.closed || subscription == null || !subscription.destination.equals(destination)) {
                    return false;
                }
                if (!legacy) {
                    validateSubscription(sessionId, principal.getUserId(), subscriptionId, subscription);
                }
                return true;
            }
        } catch (RuntimeException exception) {
            log.warn("Chat socket delivery denied: session={}, error={}", sessionId,
                exception.getClass().getSimpleName());
            return false;
        }
    }

    public void heartbeat(String sessionId) {
        Connection current = connection(sessionId);
        if (current.subscriptions.values().stream().noneMatch(subscription -> !isLegacyDestination(subscription.destination))) {
            return;
        }
        UserPrincipal principal;
        Connection connection;
        try {
            principal = authenticate(sessionId);
            connection = connection(sessionId);
        } catch (RuntimeException exception) {
            if (isAccessLost(exception) && !current.subscriptions.values().stream()
                .anyMatch(subscription -> isLegacyDestination(subscription.destination))) {
                throw exception;
            }
            // 기존 구독은 유지하고 일시적인 인증 저장소 오류는 다음 heartbeat에서 재시도한다.
            return;
        }
        synchronized (connection) {
            if (connection.closed) {
                return;
            }
            for (var entry : Map.copyOf(connection.subscriptions).entrySet()) {
                Subscription subscription = entry.getValue();
                if (subscription.room == null || isLegacyDestination(subscription.destination)) {
                    continue;
                }
                try {
                    validateSubscription(sessionId, principal.getUserId(), entry.getKey(), subscription);
                    touchRoomPresence(subscription.room, principal.getUserId(), sessionId);
                } catch (RuntimeException exception) {
                    log.warn("Chat socket subscription renewal failed: session={}, room={}, error={}", sessionId,
                        subscription.room.encoded(), exception.getClass().getSimpleName());
                }
            }
        }
    }

    public void disconnect(String sessionId) {
        if (sessionId == null) {
            return;
        }
        Connection connection = connections.get(sessionId);
        if (connection == null) {
            return;
        }
        synchronized (connection) {
            connection.closed = true;
            connections.remove(sessionId, connection);
            try {
                connection.subscriptions.values().stream()
                    .filter(subscription -> !isLegacyDestination(subscription.destination))
                    .map(Subscription::room).filter(java.util.Objects::nonNull).distinct()
                    .forEach(room -> leavePresence(room, connection.principal.getUserId(), sessionId));
                if (connection.usedLegacy) {
                    refreshLegacyStatus(connection.principal.getUserId());
                }
            } finally {
                connection.subscriptions.clear();
            }
        }
    }

    @EventListener
    public void disconnected(SessionDisconnectEvent event) {
        disconnect(event.getSessionId());
    }

    private void validateSubscription(String sessionId, Integer userId, String subscriptionId,
        Subscription subscription) {
        try {
            validateDestination(userId, subscription.destination);
        } catch (RuntimeException exception) {
            if (isAccessLost(exception)) {
                revokeSubscription(sessionId, subscriptionId, subscription.generation);
            }
            throw exception;
        }
    }

    private void touchRoomPresence(ChatRoomKey room, Integer userId, String sessionId) {
        presenceService.touch(room, userId, presenceId(sessionId));
    }

    private void refreshLegacyStatus(Integer userId) {
        // 여러 연결이 공유하는 사용자 상태는 조회와 Redis 저장까지 같은 잠금으로 처리한다.
        synchronized (legacyStateLocks[Math.floorMod(userId, legacyStateLocks.length)]) {
            Connection owner = connections.values().stream()
                .filter(connection -> !connection.closed && connection.usedLegacy
                    && userId.equals(connection.principal.getUserId())).findFirst().orElse(null);
            if (owner == null) {
                legacySessions.delete(userId, null);
                return;
            }
            Subscription active = connections.values().stream()
                .filter(connection -> !connection.closed && userId.equals(connection.principal.getUserId()))
                .flatMap(connection -> connection.subscriptions.values().stream())
                .filter(subscription -> isLegacyDestination(subscription.destination))
                .max(java.util.Comparator.comparingLong(Subscription::generation)).orElse(null);
            var state = WebSocketUserSession.of(userId, owner.principal.getDeviceToken());
            if (active != null && active.room != null) {
                state.updateStatus(WebSocketUserSessionStatus.ACTIVE_CHAT_ROOM,
                    active.room.contextId().longValue(), active.room.chatRoomId().longValue());
            } else if (active != null) {
                state.updateStatus(WebSocketUserSessionStatus.ACTIVE_CHAT_ROOM_LIST, -1L, -1L);
            }
            legacySessions.save(userId, state);
        }
    }

    private ChatRoomKey validateDestination(Integer userId, String destination) {
        if (isChatList(destination, userId)) {
            return null;
        }
        if (destination != null && destination.startsWith("/topic/chatrooms/")) {
            return chatService.validateReadAccess(userId, ChatRoomKey.parse(
                destination.substring("/topic/chatrooms/".length())).encoded());
        }
        Matcher legacy = LEGACY_ROOM.matcher(destination == null ? "" : destination);
        if (legacy.matches()) {
            var room = new ChatRoomKey(ChatDomainType.LOST_ITEM,
                Integer.valueOf(legacy.group(1)), Integer.valueOf(legacy.group(2)));
            legacyAccess.validateParticipant(room.contextId(), room.chatRoomId(), userId);
            return room;
        }
        throw denied();
    }

    private Connection connection(String sessionId) {
        Connection connection = sessionId == null ? null : connections.get(sessionId);
        if (connection == null || connection.closed) {
            throw denied();
        }
        return connection;
    }

    private void leavePresence(ChatRoomKey room, Integer userId, String sessionId) {
        try {
            presenceService.leave(room, userId, presenceId(sessionId));
        } catch (RuntimeException exception) {
            // 로컬 해제는 유지하며 공유 접속 상태는 TTL로 만료된다.
            log.warn("Chat socket presence removal failed: session={}, room={}, error={}", sessionId,
                room.encoded(), exception.getClass().getSimpleName());
        }
    }

    private static boolean isLegacyDestination(String destination) {
        return destination != null && (LEGACY_ROOM.matcher(destination).matches()
            || destination.startsWith("/topic/chatroom/list/"));
    }

    private static boolean isChatList(String destination, Integer userId) {
        return ("/topic/chatroom/list/" + userId).equals(destination);
    }

    // 실제 broker 등록까지 같은 연결 잠금으로 묶어 지연된 SUBSCRIBE가 취소된 구독을 되살리지 못하게 한다.
    void registerBrokerSubscription(String sessionId, String subscriptionId, Long generation, Runnable registration) {
        Connection connection = connections.get(sessionId);
        if (connection == null || generation == null) {
            return;
        }
        synchronized (connection) {
            Subscription current = connection.subscriptions.get(subscriptionId);
            if (!connection.closed && current != null && current.generation == generation) {
                registration.run();
            }
        }
    }

    void withConnectionLock(String sessionId, Runnable action) {
        Connection connection = connections.get(sessionId);
        if (connection == null) {
            action.run();
        } else {
            synchronized (connection) {
                action.run();
            }
        }
    }

    public record SubscriptionRevoked(String sessionId, String subscriptionId, long generation) {
    }

    private static boolean isAccessLost(RuntimeException exception) {
        if (exception instanceof AuthenticationException) {
            return true;
        }
        if (exception instanceof CustomException customException) {
            int status = customException.getErrorCode().getHttpStatus().value();
            return status == 401 || status == 403 || status == 404;
        }
        return false;
    }

    private static String presenceId(String sessionId) {
        return "ws:" + sessionId;
    }

    private static AuthenticationException denied() {
        return AuthenticationException.withDetail("인증되지 않았거나 허용되지 않은 채팅 연결입니다.");
    }

    private static final class Connection {
        private final String token;
        private final boolean cookie;
        private final UserPrincipal principal;
        private final Map<String, Subscription> subscriptions = new ConcurrentHashMap<>();
        private volatile boolean closed;
        private volatile boolean usedLegacy;

        private Connection(String token, boolean cookie, UserPrincipal principal) {
            this.token = token;
            this.cookie = cookie;
            this.principal = principal;
        }
    }

    private record Subscription(String destination, ChatRoomKey room, long generation) {
    }
}
