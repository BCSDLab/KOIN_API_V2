package in.koreatech.koin.unit.socket;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ExecutorSubscribableChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;

import in.koreatech.koin.domain.chat.model.ChatRoomKey;
import in.koreatech.koin.domain.chat.service.ChatPresenceService;
import in.koreatech.koin.domain.chat.service.ChatService;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.repository.UserRepository;
import in.koreatech.koin.domain.user.web.service.WebAuthService;
import in.koreatech.koin.global.auth.JwtProvider;
import in.koreatech.koin.global.auth.WebCsrfTokenProvider;
import in.koreatech.koin.global.auth.exception.AuthenticationException;
import in.koreatech.koin.global.code.ApiResponseCode;
import in.koreatech.koin.global.exception.CustomException;
import in.koreatech.koin.socket.config.auth.UserPrincipal;
import in.koreatech.koin.socket.interceptor.ChatOutboundInterceptor;
import in.koreatech.koin.socket.interceptor.ConnectInterceptor;
import in.koreatech.koin.socket.session.service.ChatSocketSessionRegistry;

class ChatSocketSecurityTest {
    private final JwtProvider jwt = mock(JwtProvider.class);
    private final UserRepository users = mock(UserRepository.class);
    private final WebAuthService web = mock(WebAuthService.class);
    private final WebCsrfTokenProvider csrf = mock(WebCsrfTokenProvider.class);
    private final ChatService chat = mock(ChatService.class);
    private final ChatPresenceService presence = mock(ChatPresenceService.class);
    private final WebSocketSession transport = mock(WebSocketSession.class);
    private final in.koreatech.koin.socket.session.service.WebSocketUserSessionService legacySessions =
        mock(in.koreatech.koin.socket.session.service.WebSocketUserSessionService.class);
    private final in.koreatech.koin.domain.community.lostitem.chatroom.service.LostItemChatAccessService legacyAccess =
        mock(in.koreatech.koin.domain.community.lostitem.chatroom.service.LostItemChatAccessService.class);
    private ChatSocketSessionRegistry sessions;
    private ConnectInterceptor inbound;
    private ChatOutboundInterceptor outbound;
    private final String room = "lost-item-1-2";

    @BeforeEach
    void setUp() {
        sessions = new ChatSocketSessionRegistry(jwt, web, csrf, users, chat, presence,
            mock(org.springframework.context.ApplicationEventPublisher.class), legacySessions, legacyAccess);
        when(transport.getId()).thenReturn("session");
        when(transport.isOpen()).thenReturn(true);
        sessions.transportEstablished(transport);
        inbound = new ConnectInterceptor(sessions);
        outbound = new ChatOutboundInterceptor(sessions);
        when(jwt.getUserId("mobile")).thenReturn(7);
        User user = mock(User.class);
        when(user.getId()).thenReturn(7);
        when(users.getById(7)).thenReturn(user);
        when(chat.validateReadAccess(7, room)).thenReturn(ChatRoomKey.parse(room));
    }

    @Test
    void connectRetainsPrincipalOnOriginalAccessorAndAcceptsBearerWithoutDeviceToken() {
        var headers = headers(StompCommand.CONNECT);
        headers.setNativeHeader("Authorization", "Bearer mobile");
        var message = message(headers);
        inbound.preSend(message, null);
        assertThat(headers.getUser()).isInstanceOf(UserPrincipal.class);
        assertThat(headers.getUser().getName()).isEqualTo("7");
        verify(jwt).getUserId("mobile");
    }

    @Test
    void connectAliasesRetainUserCallbackWithoutReauthenticatingIdleConnection() {
        for (StompCommand command : new StompCommand[] {StompCommand.CONNECT, StompCommand.STOMP}) {
            clearInvocations(jwt, users);
            var headers = headers(command);
            String sessionId = command.name();
            headers.setSessionId(sessionId);
            headers.setNativeHeader("Authorization", "mobile");
            var retainedPrincipal = new java.util.concurrent.atomic.AtomicReference<java.security.Principal>();
            headers.setUserChangeCallback(retainedPrincipal::set);

            inbound.preSend(message(headers), null);

            assertThat(retainedPrincipal.get()).isSameAs(headers.getUser());
            assertThat(retainedPrincipal.get().getName()).isEqualTo("7");
            verify(jwt, times(1)).getUserId("mobile");
            verify(users, times(1)).getById(7);

            var heartbeat = StompHeaderAccessor.createForHeartbeat();
            heartbeat.setSessionId(sessionId);
            heartbeat.setLeaveMutable(true);
            inbound.preSend(message(heartbeat), null);

            verify(jwt, times(1)).getUserId("mobile");
            verify(users, times(1)).getById(7);
            sessions.disconnect(sessionId);
        }
    }

    @Test
    void topicSendAndAnotherUsersListAndUnknownSubscriptionAreRejected() {
        connect();
        for (String destination : new String[] {"/topic/chatrooms/" + room, "/app/unknown"}) {
            var headers = headers(StompCommand.SEND);
            headers.setDestination(destination);
            assertThatThrownBy(() -> inbound.preSend(message(headers), null)).isInstanceOf(RuntimeException.class);
        }
        for (String destination : new String[] {"/topic/chatroom/list/8", "/topic/unknown"}) {
            var headers = headers(StompCommand.SUBSCRIBE);
            headers.setSubscriptionId("sub");
            headers.setDestination(destination);
            assertThatThrownBy(() -> inbound.preSend(message(headers), null)).isInstanceOf(RuntimeException.class);
        }
    }

    @Test
    void outboundUsesRecipientSessionEvenWhenSimpUserIsSenderOrAbsentAndChecksRevocation() {
        connect();
        subscribe("sub", "/topic/chatrooms/" + room);
        var headers = headers(StompCommand.MESSAGE);
        headers.setSubscriptionId("sub");
        headers.setDestination("/topic/chatrooms/" + room);
        headers.setUser(new UserPrincipal(99, "sender", null, null));
        assertThat(outbound.preSend(message(headers), null)).isNotNull();
        headers.setUser(null);
        assertThat(outbound.preSend(message(headers), null)).isNotNull();
        doThrow(CustomException.of(ApiResponseCode.TEAM_RECRUITMENT_CHAT_FORBIDDEN)).when(chat).validateReadAccess(7, room);
        assertThat(outbound.preSend(message(headers), null)).isNull();
        verify(chat, never()).validateReadAccess(eq(99), anyString());
    }

    @Test
    void missingRecipientSessionDoesNotFallBackToSender() {
        var headers = headers(StompCommand.MESSAGE);
        headers.setUser(new UserPrincipal(7, "sender", null, null));
        headers.setDestination("/topic/chatrooms/" + room);
        assertThat(outbound.preSend(message(headers), null)).isNull();
    }

    @Test
    void deliveredLegacyFrameDoesNotAutomaticallyMarkRead() {
        connect();
        subscribe("legacy", "/topic/chat/1/2");
        var headers = legacyDelivery();
        var channel = new ExecutorSubscribableChannel();
        channel.addInterceptor(outbound);
        var delivered = new AtomicBoolean();
        channel.subscribe(frame -> delivered.set(true));

        assertThat(channel.send(message(headers))).isTrue();

        assertThat(delivered).isTrue();
        verify(chat, never()).markRead(anyInt(), anyString(), anyLong());
        verifyNoInteractions(presence);
    }

    @Test
    void multipleSubscriptionsKeepPresenceUntilLastSubscriptionLeaves() {
        connect();
        subscribe("a", "/topic/chatrooms/" + room);
        subscribe("b", "/topic/chatrooms/" + room);
        var headers = headers(StompCommand.UNSUBSCRIBE);
        headers.setSubscriptionId("a");
        inbound.preSend(message(headers), null);
        verify(presence, never()).leave(any(), anyInt(), anyString());
        headers.setSubscriptionId("b");
        inbound.preSend(message(headers), null);
        verify(presence).leave(ChatRoomKey.parse(room), 7, "ws:session");
    }

    @Test
    void cookieConnectRequiresCsrfAndRechecksSessionOnDelivery() {
        var session = mock(in.koreatech.koin.domain.user.web.model.WebAuthSession.class);
        when(session.userId()).thenReturn(7);
        when(web.authenticate("cookie")).thenReturn(session);
        var headers = headers(StompCommand.CONNECT);
        headers.getSessionAttributes().put(
            in.koreatech.koin.socket.interceptor.ChatHandshakeInterceptor.AUTH_CONTEXT,
            new in.koreatech.koin.socket.interceptor.ChatHandshakeInterceptor.CookieContext("cookie", true));
        headers.setNativeHeader("X-CSRF-Token", "csrf");
        inbound.preSend(message(headers), null);
        verify(csrf).validate(session, "csrf");
        subscribe("sub", "/topic/chatrooms/" + room);
        when(web.authenticate("cookie")).thenThrow(AuthenticationException.withDetail("logged out"));
        var delivery = headers(StompCommand.MESSAGE);
        delivery.setDestination("/topic/chatrooms/" + room);
        delivery.setSubscriptionId("sub");
        assertThat(outbound.preSend(message(delivery), null)).isNull();
        verify(presence).leave(ChatRoomKey.parse(room), 7, "ws:session");
    }

    @Test
    void explicitBearerDoesNotAuthenticateCapturedCookie() {
        var headers = headers(StompCommand.CONNECT);
        headers.getSessionAttributes().put(
            in.koreatech.koin.socket.interceptor.ChatHandshakeInterceptor.AUTH_CONTEXT,
            new in.koreatech.koin.socket.interceptor.ChatHandshakeInterceptor.CookieContext("expired-cookie", false));
        headers.setNativeHeader("Authorization", "Bearer mobile");

        inbound.preSend(message(headers), null);

        assertThat(headers.getUser().getName()).isEqualTo("7");
        verifyNoInteractions(web, csrf);
    }

    @Test
    void invalidExplicitBearerDoesNotFallBackToCapturedCookie() {
        var headers = headers(StompCommand.CONNECT);
        headers.getSessionAttributes().put(
            in.koreatech.koin.socket.interceptor.ChatHandshakeInterceptor.AUTH_CONTEXT,
            new in.koreatech.koin.socket.interceptor.ChatHandshakeInterceptor.CookieContext("cookie", true));
        headers.setNativeHeader("Authorization", "Bearer invalid");
        when(jwt.getUserId("invalid")).thenThrow(AuthenticationException.withDetail("invalid"));

        assertThatThrownBy(() -> inbound.preSend(message(headers), null))
            .isInstanceOf(AuthenticationException.class);
        verifyNoInteractions(web, csrf);
        assertThatThrownBy(() -> sessions.authenticate("session")).isInstanceOf(RuntimeException.class);
    }

    @Test
    void cookieConnectPropagatesSessionStoreFailureWithoutRegisteringConnection() {
        var headers = headers(StompCommand.CONNECT);
        headers.getSessionAttributes().put(
            in.koreatech.koin.socket.interceptor.ChatHandshakeInterceptor.AUTH_CONTEXT,
            new in.koreatech.koin.socket.interceptor.ChatHandshakeInterceptor.CookieContext("cookie", true));
        var failure = new DataAccessResourceFailureException("redis unavailable");
        when(web.authenticate("cookie")).thenThrow(failure);

        assertThatThrownBy(() -> inbound.preSend(message(headers), null)).isSameAs(failure);
        verifyNoInteractions(csrf);
        assertThatThrownBy(() -> sessions.authenticate("session")).isInstanceOf(RuntimeException.class);
    }

    @Test
    void missingCookieCsrfDoesNotRegisterConnection() {
        var session = mock(in.koreatech.koin.domain.user.web.model.WebAuthSession.class);
        when(web.authenticate("cookie")).thenReturn(session);
        doThrow(new IllegalStateException("csrf")).when(csrf).validate(session, null);
        var headers = headers(StompCommand.CONNECT);
        headers.getSessionAttributes().put(
            in.koreatech.koin.socket.interceptor.ChatHandshakeInterceptor.AUTH_CONTEXT,
            new in.koreatech.koin.socket.interceptor.ChatHandshakeInterceptor.CookieContext("cookie", true));
        assertThatThrownBy(() -> inbound.preSend(message(headers), null)).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> sessions.authenticate("session")).isInstanceOf(RuntimeException.class);
    }

    @Test
    void deletedUserCannotSend() {
        connect();
        when(users.getById(7)).thenThrow(CustomException.of(ApiResponseCode.NOT_FOUND_USER));
        var headers = headers(StompCommand.SEND);
        headers.setDestination("/app/chatrooms/" + room + "/messages");
        assertThatThrownBy(() -> inbound.preSend(message(headers), null)).isInstanceOf(CustomException.class);
        verify(chat, never()).validateReadAccess(anyInt(), anyString());
    }

    @Test
    void legacyListDeliveryDoesNotApplyCommonRoomPolicy() {
        connect();
        subscribe("list", "/topic/chatroom/list/7");
        var headers = headers(StompCommand.MESSAGE);
        headers.setSubscriptionId("list");
        headers.setDestination("/topic/chatroom/list/7");

        assertThat(outbound.preSend(message(headers), null)).isNotNull();
        verifyNoInteractions(chat, presence, legacyAccess);
    }

    @Test
    void untrustedCookieFallbackIsRejectedBeforeAuthentication() {
        var headers = headers(StompCommand.CONNECT);
        headers.getSessionAttributes().put(
            in.koreatech.koin.socket.interceptor.ChatHandshakeInterceptor.AUTH_CONTEXT,
            new in.koreatech.koin.socket.interceptor.ChatHandshakeInterceptor.CookieContext("cookie", false));

        assertThatThrownBy(() -> inbound.preSend(message(headers), null))
            .isInstanceOf(AuthenticationException.class);
        verifyNoInteractions(web, csrf, jwt);
    }

    @Test
    void heartbeatAndClientTrafficRefreshPresenceAndDisconnectOnlyRemovesOwnSession() {
        connect();
        subscribe("sub", "/topic/chatrooms/" + room);
        clearInvocations(presence);
        var heartbeat = StompHeaderAccessor.createForHeartbeat();
        heartbeat.setSessionId("session");
        heartbeat.setLeaveMutable(true);
        inbound.preSend(message(heartbeat), null);
        verify(presence).touch(ChatRoomKey.parse(room), 7, "ws:session");
        var send = headers(StompCommand.SEND);
        send.setDestination("/app/chatrooms/" + room + "/messages");
        inbound.preSend(message(send), null);
        verify(presence, times(2)).touch(ChatRoomKey.parse(room), 7, "ws:session");
        inbound.preSend(message(headers(StompCommand.DISCONNECT)), null);
        verify(presence).leave(ChatRoomKey.parse(room), 7, "ws:session");
        sessions.disconnect("session");
        verifyNoMoreInteractions(presence);
    }

    @Test
    void expiredLegacyTokenStillAllowsSendDeliveryAndHeartbeatWithoutPresenceLease() {
        connect();
        subscribe("legacy", "/topic/chat/1/2");
        when(jwt.getUserId("mobile")).thenThrow(AuthenticationException.withDetail("expired"));
        var send = headers(StompCommand.SEND);
        send.setDestination("/app/chat/1/2");

        assertThat(inbound.preSend(message(send), null)).isNotNull();
        sessions.heartbeat("session");
        assertThat(outbound.preSend(message(legacyDelivery()), null)).isNotNull();

        verify(jwt).getUserId("mobile");
        verify(legacyAccess, times(2)).validateParticipant(1, 2, 7);
        var state = ArgumentCaptor.forClass(in.koreatech.koin.socket.session.model.WebSocketUserSession.class);
        verify(legacySessions).save(eq(7), state.capture());
        assertThat(state.getValue().getCurrentArticleId()).isEqualTo(1L);
        assertThat(state.getValue().getCurrentChatRoomId()).isEqualTo(2L);
        assertThat(state.getValue().getStatus()).isEqualTo(
            in.koreatech.koin.socket.session.model.WebSocketUserSessionStatus.ACTIVE_CHAT_ROOM);
        verifyNoInteractions(presence, chat);
    }

    @Test
    void legacySubscribeAndSendRequireDomainParticipation() {
        connect();
        doThrow(CustomException.of(ApiResponseCode.FORBIDDEN_PARTICIPANT))
            .when(legacyAccess).validateParticipant(1, 2, 7);

        assertThatThrownBy(() -> subscribe("legacy", "/topic/chat/1/2")).isInstanceOf(CustomException.class);
        var send = headers(StompCommand.SEND);
        send.setDestination("/app/chat/1/2");
        assertThatThrownBy(() -> inbound.preSend(message(send), null)).isInstanceOf(CustomException.class);
        verifyNoInteractions(legacySessions, presence);
    }

    @Test
    void disconnectDoesNotClearAnotherDeviceConnectionOrPresence() {
        connect();
        subscribe("sub", "/topic/chatrooms/" + room);
        var second = headers(StompCommand.CONNECT);
        second.setSessionId("other-session");
        second.setNativeHeader("Authorization", "mobile");
        inbound.preSend(message(second), null);
        var subscription = headers(StompCommand.SUBSCRIBE);
        subscription.setSessionId("other-session");
        subscription.setSubscriptionId("sub");
        subscription.setDestination("/topic/chatrooms/" + room);
        inbound.preSend(message(subscription), null);
        sessions.disconnect("session");
        verify(presence).leave(ChatRoomKey.parse(room), 7, "ws:session");
        verify(presence, never()).leave(ChatRoomKey.parse(room), 7, "ws:other-session");
        assertThat(sessions.canDeliver("other-session", "sub", "/topic/chatrooms/" + room)).isTrue();
    }

    @Test
    void unsupportedQueuesRejectSubscriptionsAndDirectBrokerSends() {
        assertThatThrownBy(() -> subscribe("unsupported", "/user/queue/private"))
            .isInstanceOf(RuntimeException.class);
        connect();
        for (String destination : new String[] {
            "/queue/private", "/queue/private-usersession", "/user/7/queue/private",
            "/user/8/queue/private", "/user/queue/other"
        }) {
            assertThatThrownBy(() -> subscribe("unsupported", destination)).isInstanceOf(RuntimeException.class);
        }
        for (String destination : new String[] {
            "/queue/private", "/queue/private-usersession", "/user/queue/private",
            "/user/8/queue/private", "/topic/chatrooms/" + room
        }) {
            var send = headers(StompCommand.SEND);
            send.setDestination(destination);
            assertThatThrownBy(() -> inbound.preSend(message(send), null)).isInstanceOf(RuntimeException.class);
        }
    }

    @Test
    void transientHeartbeatUserLookupRecoversOnSameSubscription() throws Exception {
        connect();
        subscribe("sub", "/topic/chatrooms/" + room);
        User user = users.getById(7);
        when(users.getById(7)).thenThrow(new DataAccessResourceFailureException("timeout"))
            .thenReturn(user);

        sessions.heartbeat("session");
        sessions.heartbeat("session");

        assertThat(sessions.canDeliver("session", "sub", "/topic/chatrooms/" + room)).isTrue();
        verify(presence, never()).leave(any(), anyInt(), anyString());
        verify(transport, never()).close(any(CloseStatus.class));
    }

    @Test
    void transientCookieSessionStoreFailureRecoversWithoutReconnectOrResubscribe() throws Exception {
        var webSession = mock(in.koreatech.koin.domain.user.web.model.WebAuthSession.class);
        when(webSession.userId()).thenReturn(7);
        when(web.authenticate("cookie")).thenReturn(webSession);
        var connect = headers(StompCommand.CONNECT);
        connect.getSessionAttributes().put(
            in.koreatech.koin.socket.interceptor.ChatHandshakeInterceptor.AUTH_CONTEXT,
            new in.koreatech.koin.socket.interceptor.ChatHandshakeInterceptor.CookieContext("cookie", true));
        connect.setNativeHeader("X-CSRF-Token", "csrf");
        inbound.preSend(message(connect), null);
        subscribe("sub", "/topic/chatrooms/" + room);
        when(web.authenticate("cookie")).thenThrow(new DataAccessResourceFailureException("redis timeout"))
            .thenReturn(webSession);

        var heartbeat = StompHeaderAccessor.createForHeartbeat();
        heartbeat.setSessionId("session");
        heartbeat.setLeaveMutable(true);
        inbound.preSend(message(heartbeat), null);
        inbound.preSend(message(heartbeat), null);
        var delivery = headers(StompCommand.MESSAGE);
        delivery.setSubscriptionId("sub");
        delivery.setDestination("/topic/chatrooms/" + room);

        assertThat(outbound.preSend(message(delivery), null)).isNotNull();
        verify(presence, never()).leave(any(), anyInt(), anyString());
        verify(transport, never()).close(any(CloseStatus.class));
    }

    @Test
    void transientAuthenticateFailureDoesNotDisconnectHealthyConnection() {
        connect();
        subscribe("sub", "/topic/chatrooms/" + room);
        User user = users.getById(7);
        when(users.getById(7)).thenThrow(new DataAccessResourceFailureException("timeout"))
            .thenReturn(user);

        assertThatThrownBy(() -> sessions.authenticate("session"))
            .isInstanceOf(DataAccessResourceFailureException.class);
        assertThat(sessions.canDeliver("session", "sub", "/topic/chatrooms/" + room)).isTrue();
        verify(presence, never()).leave(any(), anyInt(), anyString());
    }

    @Test
    void transientDeliveryAuthorizationLookupFailsClosedThenRecovers() {
        connect();
        subscribe("sub", "/topic/chatrooms/" + room);
        when(chat.validateReadAccess(7, room))
            .thenThrow(new DataAccessResourceFailureException("timeout"))
            .thenReturn(ChatRoomKey.parse(room));

        assertThat(sessions.canDeliver("session", "sub", "/topic/chatrooms/" + room)).isFalse();
        assertThat(sessions.canDeliver("session", "sub", "/topic/chatrooms/" + room)).isTrue();
        verify(presence, never()).leave(any(), anyInt(), anyString());
    }

    @Test
    void transientHeartbeatMembershipLookupKeepsSubscriptionForRetry() {
        connect();
        subscribe("sub", "/topic/chatrooms/" + room);
        when(chat.validateReadAccess(7, room))
            .thenThrow(new DataAccessResourceFailureException("timeout"))
            .thenReturn(ChatRoomKey.parse(room));
        clearInvocations(presence);

        sessions.heartbeat("session");
        verifyNoInteractions(presence);
        sessions.heartbeat("session");

        verify(presence).touch(ChatRoomKey.parse(room), 7, "ws:session");
        assertThat(sessions.canDeliver("session", "sub", "/topic/chatrooms/" + room)).isTrue();
        verify(presence, never()).leave(any(), anyInt(), anyString());
    }

    @Test
    void transientPresenceTouchFailureKeepsSubscriptionForRetry() {
        connect();
        subscribe("sub", "/topic/chatrooms/" + room);
        clearInvocations(presence);
        doThrow(new DataAccessResourceFailureException("redis timeout"))
            .doNothing().when(presence).touch(ChatRoomKey.parse(room), 7, "ws:session");

        sessions.heartbeat("session");
        sessions.heartbeat("session");

        verify(presence, times(2)).touch(ChatRoomKey.parse(room), 7, "ws:session");
        assertThat(sessions.canDeliver("session", "sub", "/topic/chatrooms/" + room)).isTrue();
        verify(presence, never()).leave(any(), anyInt(), anyString());
    }

    @Test
    void confirmedMembershipLossRemovesSubscriptionEvenIfPolicyLaterAllowsAgain() {
        connect();
        subscribe("sub", "/topic/chatrooms/" + room);
        when(chat.validateReadAccess(7, room))
            .thenThrow(CustomException.of(ApiResponseCode.TEAM_RECRUITMENT_CHAT_FORBIDDEN))
            .thenReturn(ChatRoomKey.parse(room));

        sessions.heartbeat("session");

        assertThat(sessions.canDeliver("session", "sub", "/topic/chatrooms/" + room)).isFalse();
        verify(presence).leave(ChatRoomKey.parse(room), 7, "ws:session");
    }

    @Test
    void confirmedExpiredHeartbeatClosesTransportAndDuplicateCleanupIsSafe() throws Exception {
        connect();
        subscribe("sub", "/topic/chatrooms/" + room);
        when(jwt.getUserId("mobile")).thenThrow(AuthenticationException.withDetail("expired"));
        var heartbeat = StompHeaderAccessor.createForHeartbeat();
        heartbeat.setSessionId("session");
        heartbeat.setLeaveMutable(true);

        assertThatThrownBy(() -> inbound.preSend(message(heartbeat), null))
            .isInstanceOf(AuthenticationException.class);
        sessions.transportClosed("session");
        sessions.transportClosed("session");
        doReturn(7).when(jwt).getUserId("mobile");

        assertThatThrownBy(() -> sessions.authenticate("session")).isInstanceOf(AuthenticationException.class);
        assertThat(sessions.canDeliver("session", "sub", "/topic/chatrooms/" + room)).isFalse();
        verify(transport).close(CloseStatus.POLICY_VIOLATION);
        verify(presence).leave(ChatRoomKey.parse(room), 7, "ws:session");
    }

    @Test
    void confirmedAccountRevocationClosesTransportEvenIfPresenceCleanupFails() throws Exception {
        connect();
        subscribe("sub", "/topic/chatrooms/" + room);
        when(users.getById(7)).thenThrow(CustomException.of(ApiResponseCode.NOT_FOUND_USER));
        doThrow(new DataAccessResourceFailureException("redis timeout"))
            .when(presence).leave(ChatRoomKey.parse(room), 7, "ws:session");

        assertThatThrownBy(() -> sessions.heartbeat("session")).isInstanceOf(CustomException.class);

        verify(transport).close(CloseStatus.POLICY_VIOLATION);
        assertThatThrownBy(() -> sessions.authenticate("session")).isInstanceOf(AuthenticationException.class);
    }

    @Test
    void normalDisconnectWithReceiptDoesNotForceTransportClosed() throws Exception {
        connect();
        subscribe("sub", "/topic/chatrooms/" + room);
        var disconnect = headers(StompCommand.DISCONNECT);
        disconnect.setReceipt("bye");
        var frame = message(disconnect);

        assertThat(inbound.preSend(frame, null)).isSameAs(frame);
        assertThat(disconnect.getReceipt()).isEqualTo("bye");
        verify(transport, never()).close(any(CloseStatus.class));
        verify(presence).leave(ChatRoomKey.parse(room), 7, "ws:session");
    }

    @Test
    void roomRevocationKeepsTransportAndOtherRoomSubscription() throws Exception {
        connect();
        String otherRoom = "lost-item-3-4";
        when(chat.validateReadAccess(7, otherRoom)).thenReturn(ChatRoomKey.parse(otherRoom));
        subscribe("revoked", "/topic/chatrooms/" + room);
        subscribe("retained", "/topic/chatrooms/" + otherRoom);
        when(chat.validateReadAccess(7, room))
            .thenThrow(CustomException.of(ApiResponseCode.TEAM_RECRUITMENT_CHAT_FORBIDDEN));

        sessions.heartbeat("session");

        verify(transport, never()).close(any(CloseStatus.class));
        assertThat(sessions.canDeliver("session", "revoked", "/topic/chatrooms/" + room)).isFalse();
        assertThat(sessions.canDeliver("session", "retained", "/topic/chatrooms/" + otherRoom)).isTrue();
        verify(presence).leave(ChatRoomKey.parse(room), 7, "ws:session");
        verify(presence, never()).leave(ChatRoomKey.parse(otherRoom), 7, "ws:session");
    }

    @Test
    void concurrentLegacyUnsubscribesCannotRestoreStaleActiveState() throws Exception {
        connect();
        subscribe("legacy", "/topic/chat/1/2");
        var second = headers(StompCommand.CONNECT);
        second.setSessionId("other");
        second.setNativeHeader("Authorization", "Bearer mobile");
        sessions.connect(second);
        sessions.subscribe("other", "second", "/topic/chat/1/2");
        var entered = new java.util.concurrent.CountDownLatch(1);
        var release = new java.util.concurrent.CountDownLatch(1);
        var last = new java.util.concurrent.atomic.AtomicReference<
            in.koreatech.koin.socket.session.model.WebSocketUserSessionStatus>();
        var failure = new java.util.concurrent.atomic.AtomicReference<Throwable>();
        doAnswer(invocation -> {
            var state = invocation.getArgument(1,
                in.koreatech.koin.socket.session.model.WebSocketUserSession.class);
            if (Thread.currentThread().getName().equals("unsubscribe-a")) {
                entered.countDown();
                if (!release.await(5, java.util.concurrent.TimeUnit.SECONDS)) {
                    throw new AssertionError("release timeout");
                }
            }
            last.set(state.getStatus());
            return null;
        }).when(legacySessions).save(eq(7), any());
        Thread first = new Thread(() -> {
            try { sessions.unsubscribe("session", "legacy"); }
            catch (Throwable exception) { failure.set(exception); }
        }, "unsubscribe-a");
        Thread other = new Thread(() -> {
            try { sessions.unsubscribe("other", "second"); }
            catch (Throwable exception) { failure.set(exception); }
        }, "unsubscribe-b");
        first.start();
        try {
            assertThat(entered.await(3, java.util.concurrent.TimeUnit.SECONDS)).isTrue();
            other.start();
            long deadline = System.nanoTime() + java.util.concurrent.TimeUnit.SECONDS.toNanos(3);
            while (other.getState() != Thread.State.BLOCKED && other.isAlive()
                && System.nanoTime() < deadline) {
                Thread.sleep(5);
            }
        } finally {
            release.countDown();
            first.join(3000);
            other.join(3000);
        }
        assertThat(first.isAlive() || other.isAlive()).isFalse();
        assertThat(failure.get()).isNull();
        assertThat(last.get()).isEqualTo(
            in.koreatech.koin.socket.session.model.WebSocketUserSessionStatus.ACTIVE_APP);
    }

    @Test
    void expiredCommonHeartbeatKeepsLegacySubscriptionOnSameConnection() throws Exception {
        connect();
        subscribe("legacy", "/topic/chat/1/2");
        subscribe("common", "/topic/chatrooms/" + room);
        when(jwt.getUserId("mobile")).thenThrow(AuthenticationException.withDetail("expired"));

        sessions.heartbeat("session");

        assertThat(outbound.preSend(message(legacyDelivery()), null)).isNotNull();
        assertThat(sessions.canDeliver("session", "common", "/topic/chatrooms/" + room)).isFalse();
        verify(transport, never()).close(any(CloseStatus.class));
        verify(presence).leave(ChatRoomKey.parse(room), 7, "ws:session");
    }

    private void connect() {
        var headers = headers(StompCommand.CONNECT);
        headers.setNativeHeader("Authorization", "mobile");
        inbound.preSend(message(headers), null);
    }

    private StompHeaderAccessor legacyDelivery() {
        var headers = headers(StompCommand.MESSAGE);
        headers.setSubscriptionId("legacy");
        headers.setDestination("/topic/chat/1/2");
        return headers;
    }

    private void subscribe(String id, String destination) {
        var headers = headers(StompCommand.SUBSCRIBE);
        headers.setSubscriptionId(id);
        headers.setDestination(destination);
        inbound.preSend(message(headers), null);
    }

    private StompHeaderAccessor headers(StompCommand command) {
        var headers = StompHeaderAccessor.create(command);
        headers.setSessionId("session");
        headers.setSessionAttributes(new HashMap<>());
        headers.setLeaveMutable(true);
        return headers;
    }

    private Message<byte[]> message(StompHeaderAccessor headers) {
        return MessageBuilder.createMessage(new byte[0], headers.getMessageHeaders());
    }
}
