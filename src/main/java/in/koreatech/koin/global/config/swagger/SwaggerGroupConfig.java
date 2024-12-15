package in.koreatech.koin.global.config.swagger;

import static in.koreatech.koin.global.config.swagger.ApiPackage.*;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerGroupConfig {

    @Bean
    public GroupedOpenApi loginApi() {
        return GroupedOpenApi.builder()
            .group("0. Login API")
            .pathsToMatch(LOGIN_API.getPaths())
            .build();
    }

    @Bean
    public GroupedOpenApi adminApi() {
        return createGroupedOpenApi("1. Admin API", ADMIN_API.getPaths());
    }

    @Bean
    public GroupedOpenApi businessApi() {
        return createGroupedOpenApi("2. Business API", BUSINESS_API.getPaths());
    }

    @Bean
    public GroupedOpenApi campusApi() {
        return createGroupedOpenApi("3. Campus API", CAMPUS_API.getPaths());
    }

    @Bean
    public GroupedOpenApi userApi() {
        return createGroupedOpenApi("4. User API", USER_API.getPaths());
    }

    @Bean
    public GroupedOpenApi abtestApi() {
        return createGroupedOpenApi("5. abTest API", ABTEST_API.getPaths());
    }

    @Bean
    public GroupedOpenApi bcsdApi() {
        return createGroupedOpenApi("6. bcsd API", BCSD_API.getPaths());
    }

    private GroupedOpenApi createGroupedOpenApi(String groupName, String[] packagesPath) {
        return GroupedOpenApi.builder()
            .group(groupName)
            .packagesToScan(packagesPath)
            .build();
    }
}
