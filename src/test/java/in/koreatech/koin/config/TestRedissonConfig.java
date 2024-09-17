package in.koreatech.koin.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.env.Environment;

@TestConfiguration
public class TestRedissonConfig {

    @Autowired
    private Environment environment;

    @Bean
    @Lazy
    public RedissonClient redissonClient() {
        String redisHost = environment.getProperty("spring.data.redis.host");
        String redisPort = environment.getProperty("spring.data.redis.port");

        Config config = new Config();
        config.useSingleServer()
            .setAddress("redis://" + redisHost + ":" + redisPort);

        return Redisson.create(config);
    }
}