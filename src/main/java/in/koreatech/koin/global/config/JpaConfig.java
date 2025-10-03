package in.koreatech.koin.global.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import in.koreatech.koin.global.config.repository.JpaRepository;

@Configuration
@EnableJpaAuditing
@EnableJpaRepositories(
    includeFilters = @ComponentScan.Filter(
        type = FilterType.ANNOTATION,
        value = JpaRepository.class
    )
)
@Profile("!test")
public class JpaConfig {

}
