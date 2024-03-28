package in.koreatech.koin;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import java.time.Clock;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.utility.DockerImageName;

import in.koreatech.koin.config.TestJpaConfiguration;
import in.koreatech.koin.domain.owner.model.OwnerEventListener;
import in.koreatech.koin.support.DBInitializer;
import io.restassured.RestAssured;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import({DBInitializer.class, TestJpaConfiguration.class})
@ActiveProfiles("test")
public abstract class AcceptanceTest {

    private static final String ROOT = "test";
    private static final String ROOT_PASSWORD = "1234";

    @LocalServerPort
    protected int port;

    @MockBean
    protected Clock clock;

    @MockBean
    protected OwnerEventListener ownerEventListener;

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
        mySqlContainer = (MySQLContainer)new MySQLContainer("mysql:5.7.34")
            .withDatabaseName("test")
            .withUsername(ROOT)
            .withPassword(ROOT_PASSWORD)
            .withCommand("--character-set-server=utf8mb4", "--collation-server=utf8mb4_unicode_ci");

        redisContainer = new GenericContainer<>(
            DockerImageName.parse("redis:4.0.10"))
            .withExposedPorts(6379);

        mongoContainer = new GenericContainer<>(
            DockerImageName.parse("mongo:7.0"))
            .withExposedPorts(27017);

        mySqlContainer.start();
        redisContainer.start();
        mongoContainer.start();
    }

    @BeforeEach
    void delete() {
        if (RestAssured.port == RestAssured.UNDEFINED_PORT) {
            RestAssured.port = port;
        }
        dataInitializer.clear();
    }
}
