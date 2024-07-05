package in.koreatech.koin;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import java.time.Clock;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import in.koreatech.koin.config.TestJpaConfiguration;
import in.koreatech.koin.config.TestTimeConfig;
import in.koreatech.koin.domain.bus.util.CityBusClient;
import in.koreatech.koin.domain.bus.util.CityBusRouteClient;
import in.koreatech.koin.domain.coop.model.CoopEventListener;
import in.koreatech.koin.domain.owner.model.OwnerEventListener;
import in.koreatech.koin.domain.shop.model.ShopEventListener;
import in.koreatech.koin.domain.user.model.StudentEventListener;
import in.koreatech.koin.util.TestCircuitBreakerClient;
import in.koreatech.koin.support.DBInitializer;
import io.restassured.RestAssured;
import jakarta.persistence.EntityManager;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import({DBInitializer.class, TestJpaConfiguration.class, TestTimeConfig.class})
@ActiveProfiles("test")
public abstract class AcceptanceTest {

    @LocalServerPort
    protected int port;

    @SpyBean
    protected CityBusClient cityBusClient;

    @SpyBean
    protected CityBusRouteClient cityBusRouteClient;

    @MockBean
    protected OwnerEventListener ownerEventListener;

    @MockBean
    protected StudentEventListener studentEventListener;

    @MockBean
    protected ShopEventListener shopEventListener;

    @MockBean
    protected CoopEventListener coopEventListener;

    @SpyBean
    protected TestCircuitBreakerClient testCircuitBreakerClient;

    @Autowired
    private DBInitializer dataInitializer;

    @Autowired
    protected EntityManager entityManager;

    @Autowired
    protected Clock clock;

    @BeforeEach
    void delete() {
        if (RestAssured.port == RestAssured.UNDEFINED_PORT) {
            RestAssured.port = port;
        }
        dataInitializer.clear();
    }
}