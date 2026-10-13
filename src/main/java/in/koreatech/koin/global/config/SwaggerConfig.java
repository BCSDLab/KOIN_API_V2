package in.koreatech.koin.global.config;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

import org.springdoc.core.customizers.GlobalOpenApiCustomizer;
import org.springdoc.core.properties.SwaggerUiConfigParameters;
import org.springdoc.core.properties.SwaggerUiConfigProperties;
import org.springdoc.core.properties.SwaggerUiOAuthProperties;
import org.springdoc.core.providers.ObjectMapperProvider;
import org.springdoc.webmvc.ui.SwaggerIndexPageTransformer;
import org.springdoc.webmvc.ui.SwaggerIndexTransformer;
import org.springdoc.webmvc.ui.SwaggerWelcomeCommon;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.resource.TransformedResource;

import in.koreatech.koin.global.auth.WebAuthRequestValidator;
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
    @Primary
    @ConditionalOnProperty(name = {"springdoc.swagger-ui.enabled", "springdoc.api-docs.enabled"}, matchIfMissing = true)
    public SwaggerUiConfigProperties swaggerUiConfig(SwaggerUiConfigProperties config, WebAuthProperties properties) {
        config.setWithCredentials(true);
        config.getCsrf().setEnabled(true);
        config.getCsrf().setCookieName(properties.csrfCookieName());
        config.getCsrf().setHeaderName(WebAuthRequestValidator.CSRF_HEADER);
        config.getCsrf().setUseLocalStorage(false);
        config.getCsrf().setUseSessionStorage(false);
        return config;
    }

    @Bean
    @ConditionalOnProperty(name = {"springdoc.swagger-ui.enabled", "springdoc.api-docs.enabled"}, matchIfMissing = true)
    public SwaggerIndexTransformer swaggerIndexTransformer(SwaggerUiConfigProperties config,
        SwaggerUiOAuthProperties oauthProperties, SwaggerUiConfigParameters configParameters,
        SwaggerWelcomeCommon welcome, ObjectMapperProvider objectMapperProvider) {
        SwaggerIndexPageTransformer delegate = new SwaggerIndexPageTransformer(
            config, oauthProperties, configParameters, welcome, objectMapperProvider);
        return (request, resource, chain) -> {
            Resource transformed = delegate.transform(request, resource, chain);
            if (!"swagger-initializer.js".equals(resource.getFilename())) {
                return transformed;
            }
            String initializer = transformed.getContentAsString(StandardCharsets.UTF_8).replace("plugins: [", """
                plugins: [
                    () => ({
                        wrapComponents: {
                            auths: (Original, system) => props => {
                                const definitions = props.definitions.filter((schema, name) =>
                                    name !== "WebAccessCookie" && name !== "WebRefreshCookie");
                                return definitions.size === 0 ? null :
                                    system.React.createElement(Original, {...props, definitions});
                            }
                        }
                    }),
                """);
            return new TransformedResource(transformed, initializer.getBytes(StandardCharsets.UTF_8));
        };
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
        ).addSecuritySchemes("Bot Service Authentication", new SecurityScheme()
            .type(SecurityScheme.Type.APIKEY)
            .in(SecurityScheme.In.HEADER)
            .name("X-Koin-Service-Token")
            .description("삐봇 서비스 토큰을 입력하여 인증을 활성화할 수 있습니다.")
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
