package in.koreatech.koin.global.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import in.koreatech.koin.global.config.repository.MongoRepository;

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