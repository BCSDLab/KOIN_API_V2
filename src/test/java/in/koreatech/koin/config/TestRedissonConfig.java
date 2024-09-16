package in.koreatech.koin.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class TestRedissonConfig {

    @Bean
    public RedissonClient redissonClient(
        @Value("${spring.data.redis.host}") String redisHost,

        @Value("${spring.data.redis.port}") int redisPort,

        @Value("${spring.data.redis.password:}") String redisPassword) {
        Config config = new Config();
        config.useSingleServer()
            .setAddress("redis://" + redisHost + ":" + redisPort)
            .setPassword(redisPassword.isEmpty() ? null : redisPassword);
        return Redisson.create(config);
    }
}
