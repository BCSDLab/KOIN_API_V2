package in.koreatech.koin.acceptance;

import java.time.Clock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.utility.DockerImageName;

import in.koreatech.koin.acceptance.support.DBInitializer;
import in.koreatech.koin.config.TestJpaConfiguration;
import in.koreatech.koin.config.TestRedisConfiguration;
import in.koreatech.koin.config.TestResilience4jConfig;
import in.koreatech.koin.config.TestTimeConfig;
import jakarta.persistence.EntityManager;

@SpringBootTest
@AutoConfigureMockMvc
@Import({DBInitializer.class,
    TestJpaConfiguration.class,
    TestTimeConfig.class,
    TestRedisConfiguration.class,
    TestResilience4jConfig.class})
@ActiveProfiles("test")
@Transactional
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class AcceptanceTest {

    private static final String ROOT = "test";
    private static final String ROOT_PASSWORD = "1234";

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected EntityManager entityManager;

    @Autowired
    protected Clock clock;

    @Autowired
    private DBInitializer dataInitializer;

    @Container
    protected static MySQLContainer mySqlContainer;

    @Container
    protected static GenericContainer<?> redisContainer;

    @Container
    protected static GenericContainer<?> mongoContainer;

    @DynamicPropertySource
    private static void configureProperties(final DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mySqlContainer::getJdbcUrl);
        registry.add("spring.datasource.username", () -> ROOT);
        registry.add("spring.datasource.password", () -> ROOT_PASSWORD);
        registry.add("spring.data.redis.host", redisContainer::getHost);
        registry.add("spring.data.redis.port", () -> redisContainer.getMappedPort(6379).toString());
        registry.add("spring.data.mongodb.host", mongoContainer::getHost);
        registry.add("spring.data.mongodb.port", () -> mongoContainer.getMappedPort(27017).toString());
        registry.add("spring.data.mongodb.database", () -> "test");
    }

    static {
        mySqlContainer = (MySQLContainer)new MySQLContainer("mysql:8.0.29")
            .withDatabaseName("test")
            .withUsername(ROOT)
            .withPassword(ROOT_PASSWORD)
            .withCommand("--character-set-server=utf8mb4", "--collation-server=utf8mb4_unicode_ci");

        redisContainer = new GenericContainer<>(
            DockerImageName.parse("redis:7.0.9"))
            .withExposedPorts(6379);

        mongoContainer = new GenericContainer<>(
            DockerImageName.parse("mongo:6.0.14"))
            .withExposedPorts(27017);

        mySqlContainer.start();
        redisContainer.start();
        mongoContainer.start();
    }

    @BeforeEach
    void initIncrement() {
        dataInitializer.initIncrement();
        dataInitializer.clearRedis();
    }

    protected void clear() {
        dataInitializer.clear();
    }

    protected void forceVerify(Runnable runnable) {
        TestTransaction.flagForCommit();
        TestTransaction.end();
        runnable.run();
    }
}
