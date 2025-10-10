package in.koreatech.koin.config.repository;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import in.koreatech.koin.global.marker.MongoRepositoryMarker;

@Configuration
@EnableMongoRepositories(
    basePackages = "in.koreatech.koin.domain",
    includeFilters = @ComponentScan.Filter(
        type = FilterType.ANNOTATION,
        value = MongoRepositoryMarker.class
    )
)
@Profile("!test")
public class MongoConfig {

}
