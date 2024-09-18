package in.koreatech.koin.config;

import static org.mockito.Mockito.mock;

import org.redisson.api.RedissonClient;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class TestRedissonConfig {

    @Bean
    public RedissonClient redissonClient() {
        return mock(RedissonClient.class);
    }
}
