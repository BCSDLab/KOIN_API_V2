package in.koreatech.koin.global.config.repository;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoRepositories(
    basePackages = "in.koreatech.koin",
    includeFilters = @ComponentScan.Filter(
        type = FilterType.ANNOTATION,
        value = MongoRepository.class
    )
)
@Profile("!test")
public class MongoConfig {

}
