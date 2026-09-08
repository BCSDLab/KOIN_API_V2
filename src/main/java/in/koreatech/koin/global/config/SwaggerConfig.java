package in.koreatech.koin.global.config;

import java.time.LocalDate;

import org.springdoc.core.customizers.GlobalOpenApiCustomizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import in.koreatech.koin.global.exception.ErrorResponse;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.core.converter.ResolvedSchema;
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
            .openapi("3.0.3")
            .info(apiInfo())
            .addSecurityItem(securityRequirement)
            .components(components)
            .addServersItem(server);
    }

    @Bean
    public GlobalOpenApiCustomizer errorResponseSchemaCustomizer() {
        ResolvedSchema errorResponseSchema = ModelConverters.getInstance()
            .readAllAsResolvedSchema(ErrorResponse.class);
        return openApi -> {
            Components components = openApi.getComponents();
            if (components == null) {
                components = new Components();
                openApi.setComponents(components);
            }
            components.addSchemas(ErrorResponse.class.getSimpleName(), errorResponseSchema.schema);
            errorResponseSchema.referencedSchemas.forEach(components::addSchemas);
        };
    }

    private Info apiInfo() {
        return new Info()
            .title("KOIN API")
            .description("KOIN API 문서입니다.")
            .version(LocalDate.now().toString());
    }
}
