package in.koreatech.koin;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import in.koreatech.koin.config.TestJpaConfiguration;
import in.koreatech.koin.config.TestTimeConfig;

@SpringBootTest
@ActiveProfiles("test")
@Import({TestJpaConfiguration.class, TestTimeConfig.class})
class KoinApplicationTest {

    @Test
    void contextLoads() {
    }

}
