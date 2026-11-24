package in.koreatech.koin.socket.config;

import java.util.List;

import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.converter.ByteArrayMessageConverter;
import org.springframework.messaging.converter.DefaultContentTypeResolver;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.converter.MessageConverter;
import org.springframework.messaging.converter.StringMessageConverter;
import org.springframework.messaging.simp.broker.SimpleBrokerMessageHandler;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;
import org.springframework.web.socket.handler.WebSocketHandlerDecorator;

import com.fasterxml.jackson.databind.ObjectMapper;

import in.koreatech.koin.socket.interceptor.ChatHandshakeInterceptor;
import in.koreatech.koin.socket.interceptor.ChatOutboundInterceptor;
import in.koreatech.koin.socket.interceptor.ConnectInterceptor;
import in.koreatech.koin.socket.session.service.ChatSocketSessionRegistry;
import in.koreatech.koin.socket.session.service.ChatSubscriptionRegistry;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final ConnectInterceptor connectInterceptor;
    private final ChatOutboundInterceptor outboundInterceptor;
    private final ChatHandshakeInterceptor handshakeInterceptor;
    private final ThreadPoolTaskScheduler heartbeatScheduler;
    private final ObjectMapper objectMapper;
    private final ChatSocketSessionRegistry sessions;

    public WebSocketConfig(ConnectInterceptor connectInterceptor, ChatOutboundInterceptor outboundInterceptor,
        ChatHandshakeInterceptor handshakeInterceptor,
        @Qualifier("chatHeartbeatScheduler") ThreadPoolTaskScheduler heartbeatScheduler, ObjectMapper objectMapper,
        ChatSocketSessionRegistry sessions) {
        this.connectInterceptor = connectInterceptor;
        this.outboundInterceptor = outboundInterceptor;
        this.handshakeInterceptor = handshakeInterceptor;
        this.heartbeatScheduler = heartbeatScheduler;
        this.objectMapper = objectMapper;
        this.sessions = sessions;
    }

    @Bean
    public static ThreadPoolTaskScheduler chatHeartbeatScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(1);
        scheduler.setThreadNamePrefix("chat-heartbeat-");
        scheduler.setRemoveOnCancelPolicy(true);
        return scheduler;
    }

    @Bean
    public static ChatSubscriptionRegistry chatSubscriptionRegistry(ChatSocketSessionRegistry sessions) {
        return new ChatSubscriptionRegistry(sessions);
    }

    @Bean
    public static SmartInitializingSingleton installChatSubscriptionRegistry(SimpleBrokerMessageHandler broker,
        ChatSubscriptionRegistry subscriptions) {
        return () -> broker.setSubscriptionRegistry(subscriptions);
    }

    @Override
    public boolean configureMessageConverters(List<MessageConverter> converters) {
        MappingJackson2MessageConverter jacksonConverter = new MappingJackson2MessageConverter();
        jacksonConverter.setObjectMapper(objectMapper);
        DefaultContentTypeResolver contentTypeResolver = new DefaultContentTypeResolver();
        contentTypeResolver.setDefaultMimeType(MimeTypeUtils.APPLICATION_JSON);
        jacksonConverter.setContentTypeResolver(contentTypeResolver);

        converters.add(new StringMessageConverter());
        converters.add(new ByteArrayMessageConverter());
        converters.add(jacksonConverter);
        // 기본 JSON 변환기가 추가되어 애플리케이션 직렬화 설정을 우회하지 않도록 한다.
        return false;
    }

    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration registration) {
        registration.addDecoratorFactory(handler -> new WebSocketHandlerDecorator(handler) {
            @Override
            public void afterConnectionEstablished(WebSocketSession session) throws Exception {
                try {
                    sessions.transportEstablished(session);
                    super.afterConnectionEstablished(session);
                } catch (Exception exception) {
                    sessions.transportClosed(session.getId());
                    throw exception;
                }
            }

            @Override
            public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
                try {
                    super.afterConnectionClosed(session, closeStatus);
                } finally {
                    sessions.transportClosed(session.getId());
                }
            }
        });
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic")
            .setHeartbeatValue(new long[] {10_000, 10_000})
            .setTaskScheduler(heartbeatScheduler);
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws-stomp")
            .addInterceptors(handshakeInterceptor)
            .setAllowedOriginPatterns("*");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(connectInterceptor);
    }

    @Override
    public void configureClientOutboundChannel(ChannelRegistration registration) {
        registration.interceptors(outboundInterceptor);
    }
}
