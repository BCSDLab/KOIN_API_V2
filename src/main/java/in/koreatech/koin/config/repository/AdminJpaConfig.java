package in.koreatech.koin.config.repository;

import static org.springframework.data.repository.config.BootstrapMode.LAZY;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(
    basePackages = "in.koreatech.koin.admin",
    includeFilters = @ComponentScan.Filter(
        type = FilterType.ANNOTATION,
        value = JpaRepository.class
    ),
    bootstrapMode = LAZY
)
@Profile("!test")
public class AdminJpaConfig {

}
