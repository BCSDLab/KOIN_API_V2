package in.koreatech.koin;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import in.koreatech.koin.support.DBInitializer;
import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import(DBInitializer.class)
@ActiveProfiles("test")
public abstract class AcceptanceTest {

    private static final String ROOT = "test";
    private static final String ROOT_PASSWORD = "1234";

    @LocalServerPort
    protected int port;

    @Autowired
    private DBInitializer dataInitializer;

    @Container
    protected static MySQLContainer mySqlContainer;

    @Container
    protected static GenericContainer<?> redisContainer;

    @DynamicPropertySource
    private static void configureProperties(final DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mySqlContainer::getJdbcUrl);
        registry.add("spring.datasource.username", () -> ROOT);
        registry.add("spring.datasource.password", () -> ROOT_PASSWORD);
        registry.add("spring.data.redis.host", redisContainer::getHost);
        registry.add("spring.data.redis.port", () -> redisContainer.getMappedPort(6379).toString());
    }

    static {
        mySqlContainer = (MySQLContainer) new MySQLContainer("mysql:5.7.34")
            .withDatabaseName("test")
            .withUsername(ROOT)
            .withPassword(ROOT_PASSWORD)
            .withCommand("--character-set-server=utf8mb4", "--collation-server=utf8mb4_unicode_ci");

        redisContainer = new GenericContainer<>(
            DockerImageName.parse("redis:4.0.10"))
            .withExposedPorts(6379);

        mySqlContainer.start();
        redisContainer.start();
    }

    @BeforeEach
    void delete() {
        if (RestAssured.port == RestAssured.UNDEFINED_PORT) {
            RestAssured.port = port;
        }
        dataInitializer.clear();
    }
}
