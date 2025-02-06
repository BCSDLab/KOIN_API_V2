package in.koreatech.koin.global.config.swagger;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerGroupConfig {

    @Bean
    public GroupedOpenApi loginApi() {
        String[] apiPath = new String[] {
            "/**/login"
        };

        return GroupedOpenApi.builder()
            .group("0. Login API")
            .pathsToMatch(apiPath)
            .build();
    }

    @Bean
    public GroupedOpenApi adminApi() {
        String[] packagesPath = new String[] {
            "in.koreatech.koin.admin"
        };

        return createGroupedOpenApi("1. Admin API", packagesPath);
    }

    @Bean
    public GroupedOpenApi businessApi() {
        String[] packagesPath = new String[] {
            "in.koreatech.koin.domain.owner",
            "in.koreatech.koin.domain.benefit",
            "in.koreatech.koin.domain.ownershop",
            "in.koreatech.koin.domain.shop",
            "in.koreatech.koin.domain.land"
        };

        return createGroupedOpenApi("2. Business API", packagesPath);
    }

    @Bean
    public GroupedOpenApi campusApi() {
        String[] packagesPath = new String[] {
            "in.koreatech.koin.domain.bus",
            "in.koreatech.koin.domain.community",
            "in.koreatech.koin.domain.coop",
            "in.koreatech.koin.domain.coopshop",
            "in.koreatech.koin.domain.dining",
            "in.koreatech.koin.global.socket.domain.chatroom"
        };

        return createGroupedOpenApi("3. Campus API", packagesPath);
    }

    @Bean
    public GroupedOpenApi userApi() {
        String[] packagesPath = new String[] {
            "in.koreatech.koin.domain.user",
            "in.koreatech.koin.domain.student",
            "in.koreatech.koin.domain.timetable",
            "in.koreatech.koin.domain.timetableV2",
            "in.koreatech.koin.domain.timetableV3"
        };

        return createGroupedOpenApi("4. User API", packagesPath);
    }

    @Bean
    public GroupedOpenApi abtestApi() {
        String[] packagesPath = new String[] {
            "in.koreatech.koin.admin.abtest"
        };

        return createGroupedOpenApi("5. abTest API", packagesPath);
    }

    @Bean
    public GroupedOpenApi bcsdApi() {
        String[] packagesPath = new String[] {
            "in.koreatech.koin.domain.activity",
            "in.koreatech.koin.domain.dept",
            "in.koreatech.koin.domain.kakao",
            "in.koreatech.koin.domain.member",
            "in.koreatech.koin.domain.version",
            "in.koreatech.koin.global.domain.upload",
            "in.koreatech.koin.global.domain.test",
            "in.koreatech.koin.global.domain.notification",
        };

        return createGroupedOpenApi("6. bcsd API", packagesPath);
    }

    private GroupedOpenApi createGroupedOpenApi(String groupName, String[] packagesPath) {
        return GroupedOpenApi.builder()
            .group(groupName)
            .packagesToScan(packagesPath)
            .build();
    }
}
