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

    /* response 형태가
    * [
        {
            "menu_group_id": 1,
            "menu_group_name": "메인 메뉴"
         },
      ]
    * 다음과 같이 배열로 시작할 때 사용
    * 즉, @Cacheable이 붙은 메서드가 List 같은 컬렉션을 직접 반환하는 경우 사용
    */
    private RedisCacheConfiguration listResultConfiguration() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        return RedisCacheConfiguration.defaultCacheConfig()
            .serializeKeysWith(fromSerializer(new StringRedisSerializer()))
            .serializeValuesWith(fromSerializer(new GenericJackson2JsonRedisSerializer(mapper)))
            .entryTtl(Duration.ofMinutes(1));
    }

    /* response 형태가
    * {
            "menu_group_id": 1,
            "menu_group_name": "메인 메뉴"
       }
    * 다음과 같을 때 사용
    * 즉, @Cacheable이 붙은 메서드가 단일 Wrapper DTO을 직접 반환하는 경우 사용
    */
    private RedisCacheConfiguration defaultConfiguration() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        PolymorphicTypeValidator ptv = BasicPolymorphicTypeValidator.builder()
            .allowIfBaseType(Object.class)
            .build();
        mapper.activateDefaultTyping(ptv, ObjectMapper.DefaultTyping.NON_FINAL);

        return RedisCacheConfiguration.defaultCacheConfig()
            .serializeKeysWith(fromSerializer(new StringRedisSerializer()))
            .serializeValuesWith(fromSerializer(new GenericJackson2JsonRedisSerializer(mapper)))
            .entryTtl(Duration.ofMinutes(1));
    }

    private Map<String, RedisCacheConfiguration> customConfigurationMap() {
        Map<String, RedisCacheConfiguration> customConfigurationMap = new HashMap<>();
        customConfigurationMap.put(
            CacheKey.ORDERABLE_SHOP_MENUS.getCacheNames(),
            listResultConfiguration().entryTtl(Duration.ofMinutes(CacheKey.ORDERABLE_SHOP_MENUS.getTtl()))
        );

        customConfigurationMap.put(
            CacheKey.CAMPUS_DELIVERY_ADDRESS.getCacheNames(),
            defaultConfiguration().entryTtl(Duration.ofMinutes(CacheKey.CAMPUS_DELIVERY_ADDRESS.getTtl()))
        );

        customConfigurationMap.put(
            CacheKey.RIDER_MESSAGES.getCacheNames(),
            defaultConfiguration().entryTtl(Duration.ofMinutes(CacheKey.RIDER_MESSAGES.getTtl()))
        );

        return customConfigurationMap;
    }
}
