package in.koreatech.koin.global.config.swagger;

import static in.koreatech.koin.global.config.swagger.ApiPackage.*;

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
            .pathsToMatch(LOGIN_API.getPaths())
            .build();
    }

    @Bean
    public GroupedOpenApi adminApi() {
        return GroupedOpenApi.builder()
            .group("1. Admin API")
            .packagesToScan(ADMIN_API.getPaths())
            .build();
    }

    @Bean
    public GroupedOpenApi businessOpenApi() {
        return GroupedOpenApi.builder()
            .group("2. Business API")
            .packagesToScan(BUSINESS_API.getPaths())
            .build();
    }

    @Bean
    public GroupedOpenApi campusOpenApi() {
        return GroupedOpenApi.builder()
            .group("3. Campus API")
            .packagesToScan(CAMPUS_API.getPaths())
            .build();
    }

    @Bean
    public GroupedOpenApi userOpenApi() {
        return GroupedOpenApi.builder()
            .group("4. User API")
            .packagesToScan(USER_API.getPaths())
            .build();
    }

    @Bean
    public GroupedOpenApi abtestApi() {
        return GroupedOpenApi.builder()
            .group("5. abTest API")
            .packagesToScan(ABTEST_API.getPaths())
            .build();
    }

    @Bean
    public GroupedOpenApi bcsdApi() {
        return GroupedOpenApi.builder()
            .group("6. bcsd API")
            .packagesToScan(BCSD_API.getPaths())
            .build();
    }
}
