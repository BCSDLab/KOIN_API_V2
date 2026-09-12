package in.koreatech.koin.mcp.service;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springdoc.core.customizers.SpringDocCustomizers;
import org.springdoc.core.models.GroupedOpenApi;
import org.springdoc.core.properties.SpringDocConfigProperties;
import org.springdoc.core.providers.SpringDocProviders;
import org.springdoc.core.service.AbstractRequestService;
import org.springdoc.core.service.GenericResponseService;
import org.springdoc.core.service.OpenAPIService;
import org.springdoc.core.service.OperationService;
import org.springdoc.webmvc.api.OpenApiWebMvcResource;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import in.koreatech.koin.mcp.McpConstants;
import in.koreatech.koin.mcp.exception.EndpointSpecException;
import io.swagger.v3.oas.models.OpenAPI;

@Component
@ConditionalOnProperty(name = McpConstants.SERVER_ENABLED_PROPERTY, havingValue = "true")
public class McpOpenApiProvider {

    private final Map<String, ExposedOpenApiResource> resources;

    public McpOpenApiProvider(
        List<GroupedOpenApi> groupedOpenApis,
        ObjectFactory<OpenAPIService> openApiServiceFactory,
        AbstractRequestService requestService,
        GenericResponseService responseService,
        OperationService operationService,
        SpringDocConfigProperties springDocConfigProperties,
        SpringDocProviders springDocProviders
    ) {
        OpenApiResourceDependencies dependencies = new OpenApiResourceDependencies(
            openApiServiceFactory,
            requestService,
            responseService,
            operationService,
            springDocConfigProperties,
            springDocProviders
        );
        this.resources = groupedOpenApis.stream()
            .collect(Collectors.toMap(
                GroupedOpenApi::getGroup,
                groupedOpenApi -> new ExposedOpenApiResource(
                    groupedOpenApi.getGroup(),
                    dependencies,
                    groupCustomizers(groupedOpenApi)
                )
            ));
    }

    public OpenAPI getOpenApi(String group) {
        ExposedOpenApiResource resource = resources.get(group);
        if (resource == null) {
            throw new EndpointSpecException("OPENAPI_GROUP_NOT_FOUND", "No OpenAPI resource found.");
        }
        return resource.getOpenApi();
    }

    private SpringDocCustomizers groupCustomizers(GroupedOpenApi groupedOpenApi) {
        return new SpringDocCustomizers(
            Optional.of(groupedOpenApi.getOpenApiCustomizers()),
            Optional.of(groupedOpenApi.getOperationCustomizers()),
            Optional.of(groupedOpenApi.getRouterOperationCustomizers()),
            Optional.of(groupedOpenApi.getOpenApiMethodFilters())
        );
    }

    private record OpenApiResourceDependencies(
        ObjectFactory<OpenAPIService> openApiServiceFactory,
        AbstractRequestService requestService,
        GenericResponseService responseService,
        OperationService operationService,
        SpringDocConfigProperties springDocConfigProperties,
        SpringDocProviders springDocProviders
    ) {
    }

    private static class ExposedOpenApiResource extends OpenApiWebMvcResource {

        private ExposedOpenApiResource(
            String groupName,
            OpenApiResourceDependencies dependencies,
            SpringDocCustomizers springDocCustomizers
        ) {
            super(
                groupName,
                dependencies.openApiServiceFactory(),
                dependencies.requestService(),
                dependencies.responseService(),
                dependencies.operationService(),
                dependencies.springDocConfigProperties(),
                dependencies.springDocProviders(),
                springDocCustomizers
            );
        }

        private OpenAPI getOpenApi() {
            return super.getOpenApi(Locale.getDefault());
        }
    }
}
