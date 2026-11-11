package in.koreatech.koin.unit.socket;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;
import org.springframework.web.socket.handler.WebSocketHandlerDecoratorFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import in.koreatech.koin.domain.chat.service.ChatPresenceService;
import in.koreatech.koin.domain.chat.service.ChatService;
import in.koreatech.koin.domain.user.repository.UserRepository;
import in.koreatech.koin.domain.user.web.service.WebAuthService;
import in.koreatech.koin.global.auth.JwtProvider;
import in.koreatech.koin.global.auth.WebCsrfTokenProvider;
import in.koreatech.koin.socket.config.WebSocketConfig;
import in.koreatech.koin.socket.interceptor.ChatHandshakeInterceptor;
import in.koreatech.koin.socket.interceptor.ChatOutboundInterceptor;
import in.koreatech.koin.socket.interceptor.ConnectInterceptor;
import in.koreatech.koin.socket.session.service.ChatSocketSessionRegistry;

class ChatSocketTransportLifecycleTest {
    private final ChatSocketSessionRegistry sessions = new ChatSocketSessionRegistry(mock(JwtProvider.class),
        mock(WebAuthService.class), mock(WebCsrfTokenProvider.class), mock(UserRepository.class),
        mock(ChatService.class), mock(ChatPresenceService.class),
        mock(org.springframework.context.ApplicationEventPublisher.class),
        mock(in.koreatech.koin.socket.session.service.WebSocketUserSessionService.class),
        mock(in.koreatech.koin.domain.community.lostitem.chatroom.service.LostItemChatAccessService.class));
    private final WebSocketHandler delegate = mock(WebSocketHandler.class);
    private final WebSocketSession transport = mock(WebSocketSession.class);
    private WebSocketHandler handler;

    @BeforeEach
    void setUp() {
        when(transport.getId()).thenReturn("session");
        var config = new WebSocketConfig(mock(ConnectInterceptor.class), mock(ChatOutboundInterceptor.class),
            mock(ChatHandshakeInterceptor.class), mock(ThreadPoolTaskScheduler.class), new ObjectMapper(), sessions);
        var registration = mock(WebSocketTransportRegistration.class);
        config.configureWebSocketTransport(registration);
        var factory = ArgumentCaptor.forClass(WebSocketHandlerDecoratorFactory.class);
        verify(registration).addDecoratorFactory(factory.capture());
        handler = factory.getValue().decorate(delegate);
    }

    @Test
    void closeBeforeStompConnectRemovesTransportAndDuplicateCloseIsSafe() throws Exception {
        handler.afterConnectionEstablished(transport);
        assertThat(transports()).containsEntry("session", transport);

        handler.afterConnectionClosed(transport, CloseStatus.NORMAL);
        handler.afterConnectionClosed(transport, CloseStatus.NORMAL);

        assertThat(transports()).isEmpty();
        verify(delegate).afterConnectionEstablished(transport);
        verify(transport, never()).close(any(CloseStatus.class));
    }

    @Test
    void establishmentFailureDoesNotRetainPreConnectTransport() throws Exception {
        doThrow(new IllegalStateException("establishment failed")).when(delegate)
            .afterConnectionEstablished(transport);

        assertThatThrownBy(() -> handler.afterConnectionEstablished(transport))
            .isInstanceOf(IllegalStateException.class);

        assertThat(transports()).isEmpty();
    }

    @Test
    void closeCallbackFailureStillRemovesTransport() throws Exception {
        handler.afterConnectionEstablished(transport);
        doThrow(new IllegalStateException("close callback failed")).when(delegate)
            .afterConnectionClosed(transport, CloseStatus.BAD_DATA);

        assertThatThrownBy(() -> handler.afterConnectionClosed(transport, CloseStatus.BAD_DATA))
            .isInstanceOf(IllegalStateException.class);

        assertThat(transports()).isEmpty();
    }

    @SuppressWarnings("unchecked")
    private Map<String, WebSocketSession> transports() {
        return (Map<String, WebSocketSession>)ReflectionTestUtils.getField(sessions, "transports");
    }
}
