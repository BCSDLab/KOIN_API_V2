package in.koreatech.koin.acceptance;

import java.time.Clock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
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

import in.koreatech.koin.config.TestJpaConfig;
import in.koreatech.koin.config.TestRedisConfig;
import in.koreatech.koin.config.TestResilience4jConfig;
import in.koreatech.koin.config.TestTimeConfig;
import in.koreatech.koin.domain.bus.service.express.client.PublicExpressBusClient;
import in.koreatech.koin.domain.bus.service.express.client.StaticExpressBusClient;
import in.koreatech.koin.domain.bus.service.express.client.TmoneyExpressBusClient;
import in.koreatech.koin.domain.notification.eventlistener.ArticleKeywordEventListener;
import in.koreatech.koin.domain.notification.eventlistener.CoopEventListener;
import in.koreatech.koin.infrastructure.naver.service.NaverSmsService;
import in.koreatech.koin.infrastructure.slack.eventlistener.OwnerEventListener;
import in.koreatech.koin.infrastructure.slack.eventlistener.ReviewEventListener;
import in.koreatech.koin.domain.notification.eventlistener.ShopEventListener;
import in.koreatech.koin.infrastructure.slack.eventlistener.StudentEventListener;
import in.koreatech.koin.acceptance.support.DBInitializer;
import in.koreatech.koin.acceptance.util.TestCircuitBreakerClient;
import jakarta.persistence.EntityManager;

@SpringBootTest
@AutoConfigureMockMvc
@Import({DBInitializer.class,
    TestJpaConfig.class,
    TestTimeConfig.class,
    TestRedisConfig.class,
    TestResilience4jConfig.class})
@ActiveProfiles("test")
@Transactional
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class AcceptanceTest {

    private static final String ROOT = "test";
    private static final String ROOT_PASSWORD = "1234";

    @Autowired
    public MockMvc mockMvc;

    @MockBean
    protected OwnerEventListener ownerEventListener;

    @MockBean
    protected ReviewEventListener reviewEventListener;

    @MockBean
    protected StudentEventListener studentEventListener;

    @MockBean
    protected ShopEventListener shopEventListener;

    @MockBean
    protected CoopEventListener coopEventListener;

    @MockBean
    protected ArticleKeywordEventListener articleKeywordEventListener;

    @MockBean
    protected PublicExpressBusClient publicExpressBusClient;

    @MockBean
    protected TmoneyExpressBusClient tmoneyExpressBusClient;

    @MockBean
    protected StaticExpressBusClient staticExpressBusClient;

    @SpyBean
    protected TestCircuitBreakerClient testCircuitBreakerClient;

    @MockBean
    protected NaverSmsService naverSmsService;

    @Autowired
    private DBInitializer dataInitializer;

    @Autowired
    protected EntityManager entityManager;

    @Autowired
    protected Clock clock;

    @Container
    static final MySQLContainer<?> mySqlContainer = new MySQLContainer<>("mysql:8.0.29")
        .withDatabaseName("test")
        .withUsername(ROOT)
        .withPassword(ROOT_PASSWORD)
        .withCommand("--character-set-server=utf8mb4", "--collation-server=utf8mb4_unicode_ci")
        .withReuse(true);

    @Container
    static final GenericContainer<?> redisContainer = new GenericContainer<>(DockerImageName.parse("redis:7.0.9"))
        .withExposedPorts(6379)
        .withReuse(true);

    @Container
    static final GenericContainer<?> mongoContainer = new GenericContainer<>(DockerImageName.parse("mongo:6.0.14"))
        .withExposedPorts(27017)
        .withReuse(true);

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
