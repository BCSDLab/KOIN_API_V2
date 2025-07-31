package in.koreatech.koin.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@TestConfiguration
@EnableJpaAuditing(dateTimeProviderRef = "dateTimeProvider")
public class TestJpaConfig {

}
