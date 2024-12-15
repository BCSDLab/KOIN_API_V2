package in.koreatech.koin.global.config.swagger;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;

@Configuration
public class SwaggerConfig {

    private final String serverUrl;

    public SwaggerConfig(
        @Value("${swagger.server-url}") String serverUrl
    ) {
        this.serverUrl = serverUrl;
    }

    @Bean
    public OpenAPI openAPI() {
        String jwt = "Jwt Authentication";
        SecurityRequirement securityRequirement = new SecurityRequirement().addList(jwt);
        Components components = new Components().addSecuritySchemes(jwt, new SecurityScheme()
            .name(jwt)
            .type(SecurityScheme.Type.HTTP)
            .scheme("Bearer")
            .description("토큰값을 입력하여 인증을 활성화할 수 있습니다.")
            .bearerFormat("JWT")
        );
        Server server = new Server();
        server.setUrl(serverUrl);
        return new OpenAPI()
            .openapi("3.1.0")
            .info(apiInfo())
            .addSecurityItem(securityRequirement)
            .components(components)
            .addServersItem(server);
    }

    private Info apiInfo() {
        return new Info()
            .title("KOIN API")
            .description("KOIN API 문서입니다.")
            .version("0.0.1");
    }

    @Bean
    public GroupedOpenApi loginOpenApi() {
        return GroupedOpenApi.builder()
            .group("0. Login API")
            .pathsToMatch("/**/login")
            .build();
    }

    @Bean
    public GroupedOpenApi adminApi() {
        String packagePath = "in.koreatech.koin.admin";

        return GroupedOpenApi.builder()
            .group("1. Admin API")
            .packagesToScan(packagePath)
            .build();
    }

    @Bean
    public GroupedOpenApi businessOpenApi() {
        String[] packagePath = {
            "in.koreatech.koin.domain.owner",
            "in.koreatech.koin.domain.benefit",
            "in.koreatech.koin.domain.ownershop",
            "in.koreatech.koin.domain.shop",
            "in.koreatech.koin.domain.land",
        };

        return GroupedOpenApi.builder()
            .group("2. Business API")
            .packagesToScan(packagePath)
            .build();
    }

    @Bean
    public GroupedOpenApi campusOpenApi() {
        String[] packagePath = {
            "in.koreatech.koin.domain.bus",
            "in.koreatech.koin.domain.community",
            "in.koreatech.koin.domain.coop",
            "in.koreatech.koin.domain.coopshop",
            "in.koreatech.koin.domain.dining",
            "in.koreatech.koin.domain.coop",
        };

        return GroupedOpenApi.builder()
            .group("3. Campus API")
            .packagesToScan(packagePath)
            .build();
    }

    @Bean
    public GroupedOpenApi userOpenApi() {
        String[] packagePath = {
            "in.koreatech.koin.domain.user",
            "in.koreatech.koin.domain.student",
            "in.koreatech.koin.domain.timetable",
            "in.koreatech.koin.domain.timetableV2",
        };

        return GroupedOpenApi.builder()
            .group("4. User API")
            .packagesToScan(packagePath)
            .build();
    }

    @Bean
    public GroupedOpenApi abtestApi() {
        String packagePath = "in.koreatech.koin.admin.abtest";

        return GroupedOpenApi.builder()
            .group("5. abTest API")
            .packagesToScan(packagePath)
            .build();
    }

    @Bean
    public GroupedOpenApi bcsdApi() {
        String[] packagePath = {
            "in.koreatech.koin.domain.activity",
            "in.koreatech.koin.domain.dept",
            "in.koreatech.koin.domain.kakao",
            "in.koreatech.koin.domain.member",
            "in.koreatech.koin.domain.version",
        };

        return GroupedOpenApi.builder()
            .group("6. bcsd API")
            .packagesToScan(packagePath)
            .build();
    }
}
