package in.koreatech.koin._common.cache;

import static org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair.fromSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@Configuration
@Profile("!test")
public class CacheConfig {

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        return RedisCacheManager.RedisCacheManagerBuilder.fromConnectionFactory(connectionFactory)
            .cacheDefaults(defaultConfiguration())
            .withInitialCacheConfigurations(customConfigurationMap())
            .build();
    }

    private RedisCacheConfiguration defaultConfiguration() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        PolymorphicTypeValidator ptv = BasicPolymorphicTypeValidator.builder()
            .allowIfBaseType(Object.class)
            .build();
        mapper.activateDefaultTyping(ptv, ObjectMapper.DefaultTyping.EVERYTHING);

        return RedisCacheConfiguration.defaultCacheConfig()
            .serializeKeysWith(fromSerializer(new StringRedisSerializer()))
            .serializeValuesWith(fromSerializer(new GenericJackson2JsonRedisSerializer(mapper)))
            .entryTtl(Duration.ofMinutes(1));
    }

    private Map<String, RedisCacheConfiguration> customConfigurationMap() {
        Map<String, RedisCacheConfiguration> customConfigurationMap = new HashMap<>();
        customConfigurationMap.put(
            CacheKey.ORDERABLE_SHOP_MENUS.getCacheNames(),
            defaultConfiguration().entryTtl(Duration.ofMinutes(CacheKey.ORDERABLE_SHOP_MENUS.getTtl()))
        );

        customConfigurationMap.put(
            CacheKey.CAMPUS_DELIVERY_ADDRESS.getCacheNames(),
            defaultConfiguration().entryTtl(Duration.ofMinutes(CacheKey.CAMPUS_DELIVERY_ADDRESS.getTtl()))
        );

        customConfigurationMap.put(
            CacheKey.RIDER_MESSAGES.getCacheNames(),
            defaultConfiguration().entryTtl(Duration.ofMinutes(CacheKey.RIDER_MESSAGES.getTtl()))
        );

        customConfigurationMap.put(
            CacheKey.ORDERABLE_SHOP_INFO_SUMMARY.getCacheNames(),
            defaultConfiguration().entryTtl(Duration.ofMinutes(CacheKey.ORDERABLE_SHOP_INFO_SUMMARY.getTtl()))
        );

        return customConfigurationMap;
    }
}
