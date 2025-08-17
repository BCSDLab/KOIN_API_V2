package in.koreatech.koin.global.config;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import in.koreatech.koin.global.code.ApiResponseCodesOperationCustomizer;

@Configuration
public class SwaggerGroupConfig {

    private final ApiResponseCodesOperationCustomizer customizer;

    public SwaggerGroupConfig(ApiResponseCodesOperationCustomizer customizer) {
        this.customizer = customizer;
    }

    @Bean
    public GroupedOpenApi loginApi() {
        return GroupedOpenApi.builder()
            .group("0. Login API")
            .pathsToMatch("/**/login")
            .addOperationCustomizer(customizer)
            .build();
    }

    @Bean
    public GroupedOpenApi adminApi() {
        return createGroupedOpenApi(
            "1. Admin API",
            new String[] { "in.koreatech.koin.admin" });
    }

    @Bean
    public GroupedOpenApi businessApi() {
        return createGroupedOpenApi(
            "2. Business API",
            new String[] {
                "in.koreatech.koin.domain.owner",
                "in.koreatech.koin.domain.benefit",
                "in.koreatech.koin.domain.ownershop",
                "in.koreatech.koin.domain.shop",
                "in.koreatech.koin.domain.land",
            });
    }

    @Bean
    public GroupedOpenApi campusApi() {
        return createGroupedOpenApi(
            "3. Campus API",
            new String[] {
                "in.koreatech.koin.domain.bus",
                "in.koreatech.koin.domain.community.article",
                "in.koreatech.koin.domain.community.keyword",
                "in.koreatech.koin.domain.coop",
                "in.koreatech.koin.domain.coopshop",
                "in.koreatech.koin.domain.dining",
                "in.koreatech.koin.domain.banner",
                "in.koreatech.koin.domain.club",
            });
    }

    @Bean
    public GroupedOpenApi userApi() {
        return createGroupedOpenApi(
            "4. User API",
            new String[] {
                "in.koreatech.koin.domain.user",
                "in.koreatech.koin.domain.student",
                "in.koreatech.koin.domain.timetable",
                "in.koreatech.koin.domain.timetableV2",
                "in.koreatech.koin.domain.timetableV3",
                "in.koreatech.koin.domain.dept",
                "in.koreatech.koin.domain.graduation",
            });
    }

    @Bean
    public GroupedOpenApi abtestApi() {
        return createGroupedOpenApi(
            "5. abTest API",
            new String[] { "in.koreatech.koin.admin.abtest" });
    }

    @Bean
    public GroupedOpenApi socketApi() {
        return createGroupedOpenApi(
            "6. chat API",
            new String[] { "in.koreatech.koin.domain.community.lostitem.chatroom" });
    }

    @Bean
    public GroupedOpenApi bcsdApi() {
        return createGroupedOpenApi(
            "7. bcsd API",
            new String[] {
                "in.koreatech.koin.domain.activity",
                "in.koreatech.koin.domain.member",
                "in.koreatech.koin.domain.version",
                "in.koreatech.koin.domain.notification",
                "in.koreatech.koin.domain.test",
                "in.koreatech.koin.domain.kakaobot",
                "in.koreatech.koin.domain.upload",
            });
    }

    @Bean
    public GroupedOpenApi orderApi() {
        return createGroupedOpenApi(
            "8. Business Order API",
            new String[] { "in.koreatech.koin.domain.order" });
    }

    private GroupedOpenApi createGroupedOpenApi(
        String groupName,
        String[] packagesPath) {
        return GroupedOpenApi.builder()
            .group(groupName)
            .packagesToScan(packagesPath)
            .addOperationCustomizer(customizer)
            .build();
    }
}
