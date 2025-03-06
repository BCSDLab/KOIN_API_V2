package in.koreatech.koin.socket.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import in.koreatech.koin.socket.interceptor.ConnectInterceptor;
import in.koreatech.koin.socket.interceptor.DisconnectInterceptor;
import in.koreatech.koin.socket.interceptor.SubscribeInterceptor;
import in.koreatech.koin.socket.interceptor.UnsubscribeInterceptor;
import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final ConnectInterceptor connectInterceptor;
    private final DisconnectInterceptor disconnectInterceptor;
    private final SubscribeInterceptor subscribeInterceptor;
    private final UnsubscribeInterceptor unsubscribeInterceptor;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic");
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws-stomp")
            .setAllowedOriginPatterns("*");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(connectInterceptor, subscribeInterceptor, unsubscribeInterceptor, disconnectInterceptor);
    }
}
