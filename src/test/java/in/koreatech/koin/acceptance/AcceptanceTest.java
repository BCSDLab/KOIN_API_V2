package in.koreatech.koin.acceptance;

import org.junit.jupiter.api.AfterAll;
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

import in.koreatech.koin.acceptance.support.DBInitializer;
import in.koreatech.koin.acceptance.support.DirectRoomSaveBarrier;
import in.koreatech.koin.acceptance.util.TestCircuitBreakerClient;
import in.koreatech.koin.config.TestClock;
import in.koreatech.koin.config.TestJpaConfig;
import in.koreatech.koin.config.TestRedisConfig;
import in.koreatech.koin.config.TestResilience4jConfig;
import in.koreatech.koin.config.TestTimeConfig;
import in.koreatech.koin.domain.bus.service.express.client.PublicExpressBusClient;
import in.koreatech.koin.domain.bus.service.express.client.StaticExpressBusClient;
import in.koreatech.koin.domain.bus.service.express.client.TmoneyExpressBusClient;
import in.koreatech.koin.domain.dining.repository.DiningReportRepository;
import in.koreatech.koin.domain.dining.repository.DiningReportSequenceRepository;
import in.koreatech.koin.domain.notification.eventlistener.CoopEventListener;
import in.koreatech.koin.domain.notification.eventlistener.KeywordEventListener;
import in.koreatech.koin.domain.notification.eventlistener.ShopEventListener;
import in.koreatech.koin.domain.team.recruitment.scheduler.TeamRecruitmentDeadlineScheduler;
import in.koreatech.koin.infrastructure.fcm.FcmClient;
import in.koreatech.koin.infrastructure.naver.service.NaverSmsService;
import in.koreatech.koin.infrastructure.s3.client.S3ClientWrapper;
import in.koreatech.koin.infrastructure.slack.eventlistener.OwnerEventListener;
import in.koreatech.koin.infrastructure.slack.eventlistener.ReviewEventListener;
import in.koreatech.koin.infrastructure.slack.eventlistener.StudentEventListener;
import jakarta.persistence.EntityManager;

@SpringBootTest
@AutoConfigureMockMvc
@Import({DBInitializer.class,
    DirectRoomSaveBarrier.class,
    TestJpaConfig.class,
    TestTimeConfig.class,
    TestRedisConfig.class,
    TestResilience4jConfig.class})
// 하위 클래스가 mock/spy 구성을 따로 선언하면 Spring 컨텍스트가 새로 뜬다. 필요한 대체 빈은 여기서만 선언한다.
@MockBean(TeamRecruitmentDeadlineScheduler.class)
@SpyBean({FcmClient.class, S3ClientWrapper.class, DiningReportRepository.class, DiningReportSequenceRepository.class})
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
    protected KeywordEventListener articleKeywordEventListener;

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
    protected TestClock clock;

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

    @AfterAll
    void resetClock() {
        clock.reset();
    }

    protected void clear() {
        dataInitializer.clear();
        dataInitializer.initIncrement();
    }

    protected void forceVerify(Runnable runnable) {
        TestTransaction.flagForCommit();
        TestTransaction.end();
        runnable.run();
    }
}
