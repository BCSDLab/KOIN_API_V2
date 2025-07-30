package in.koreatech.koin;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import in.koreatech.koin.config.TestJpaConfig;
import in.koreatech.koin.config.TestRedisConfig;
import in.koreatech.koin.config.TestTimeConfig;

@SpringBootTest
@ActiveProfiles("test")
@Import({TestJpaConfig.class, TestTimeConfig.class, TestRedisConfig.class})
class KoinApplicationTest {

    @Test
    void contextLoads() {
    }

}
