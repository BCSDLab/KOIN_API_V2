package in.koreatech.koin.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.data.redis.core.RedisKeyValueAdapter;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;

@EnableRedisRepositories(enableKeyspaceEvents = RedisKeyValueAdapter.EnableKeyspaceEvents.ON_STARTUP)
@TestConfiguration
public class TestRedisConfiguration {
}
