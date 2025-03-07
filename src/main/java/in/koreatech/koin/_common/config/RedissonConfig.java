package in.koreatech.koin._common.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("!test")
public class RedissonConfig {

    @Value("${spring.data.redis.host}")
    private String redisHost;

    @Value("${spring.data.redis.port}")
    private int redisPort;

    @Value("${spring.data.redis.password:}")
    private String redisPassword;

    private static final String REDISSION_HOST_PREFIX = "redis://";

    @Bean
    public RedissonClient redissionClient() {
        Config config = new Config();
        config.useSingleServer()
            .setAddress(REDISSION_HOST_PREFIX + redisHost + ":" + redisPort)
            .setPassword(redisPassword.isEmpty() ? null : redisPassword);
        return Redisson.create(config);
    }
}
