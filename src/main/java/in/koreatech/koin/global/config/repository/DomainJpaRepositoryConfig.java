package in.koreatech.koin.global.config.repository;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(
    basePackages = "in.koreatech.koin.domain",
    includeFilters = @ComponentScan.Filter(
        type = FilterType.ANNOTATION,
        value = JpaRepository.class
    )
)
@Profile("!test")
public class DomainJpaRepositoryConfig {

}
