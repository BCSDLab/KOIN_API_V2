package in.koreatech.koin.domain.chat.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

import in.koreatech.koin.domain.chat.realtime.ChatRealtimePublisher;
import in.koreatech.koin.domain.chat.realtime.ChatRealtimeRedisListener;

@Configuration
public class ChatRedisConfig {
    @Bean
    public RedisMessageListenerContainer chatRedisMessageListenerContainer(
        RedisConnectionFactory connectionFactory, ChatRealtimeRedisListener listener
    ) {
        var container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(listener, new ChannelTopic(ChatRealtimePublisher.CHANNEL));
        return container;
    }
}
