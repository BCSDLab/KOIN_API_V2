package in.koreatech.koin.mcp.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import in.koreatech.koin.global.code.Deprecation;
import in.koreatech.koin.mcp.McpConstants;
import in.koreatech.koin.mcp.dto.endpoint.EndpointDescription;
import in.koreatech.koin.mcp.dto.endpoint.EndpointSummary;
import in.koreatech.koin.mcp.dto.endpoint.FindEndpointsResponse;
import in.koreatech.koin.mcp.dto.endpoint.ReplacedBy;
import in.koreatech.koin.mcp.dto.endpoint.request.EndpointParameter;
import in.koreatech.koin.mcp.dto.endpoint.request.EndpointParameters;
import in.koreatech.koin.mcp.dto.endpoint.request.EndpointRequestSpec;
import in.koreatech.koin.mcp.dto.endpoint.response.EndpointResponse;
import in.koreatech.koin.mcp.dto.endpoint.response.EndpointResponseSpec;
import in.koreatech.koin.mcp.dto.schema.EndpointSchema;
import in.koreatech.koin.mcp.exception.EndpointSpecException;
import in.koreatech.koin.mcp.model.DeprecatedFilter;
import in.koreatech.koin.mcp.model.EndpointEntry;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.responses.ApiResponse;

@Service
@ConditionalOnProperty(name = McpConstants.SERVER_ENABLED_PROPERTY, havingValue = "true")
public class EndpointSpecService {

    private static final String APPLICATION_JSON = "application/json";

    private final EndpointCatalog endpointCatalog;
    private final McpOpenApiProvider openApiProvider;
    private final EndpointSchemaMapper schemaMapper;

    public EndpointSpecService(
        EndpointCatalog endpointCatalog,
        McpOpenApiProvider openApiProvider,
        EndpointSchemaMapper schemaMapper
    ) {
        this.endpointCatalog = endpointCatalog;
        this.openApiProvider = openApiProvider;
        this.schemaMapper = schemaMapper;
    }

    public FindEndpointsResponse findEndpoints(String query, String group, DeprecatedFilter deprecated) {
        List<EndpointSummary> items = endpointCatalog.findAll(query, group, deprecated).stream()
            .map(this::toSummary)
            .distinct()
            .sorted(Comparator
                .comparing(EndpointSummary::group)
                .thenComparing(EndpointSummary::path)
                .thenComparing(EndpointSummary::method))
            .toList();

        return new FindEndpointsResponse(items);
    }

    public EndpointDescription getEndpointDescription(String group, String method, String path) {
        EndpointEntry entry = endpointCatalog.findEndpoint(group, method, path);
        Operation operation = entry.operation();
        Deprecation deprecation = entry.deprecation();

        return new EndpointDescription(
            endpointCatalog.displayGroup(entry.group()),
            entry.method(),
            entry.path(),
            operationId(operation),
            operation == null ? "" : nullToEmpty(operation.getSummary()),
            operation == null ? "" : nullToEmpty(operation.getDescription()),
            entry.tags(),
            entry.deprecated(),
            deprecation == null ? null : deprecation.reason(),
            replacedBy(deprecation),
            entry.authRequired()
        );
    }

    public EndpointRequestSpec getEndpointRequestSpec(String group, String method, String path) {
        EndpointEntry entry = endpointCatalog.findEndpoint(group, method, path);
        OpenAPI openAPI = openApiProvider.getOpenApi(entry.group());
        Operation operation = openApiOperation(openAPI, entry);

        List<EndpointParameter> pathParameters = new ArrayList<>();
        List<EndpointParameter> queryParameters = new ArrayList<>();
        List<EndpointParameter> headerParameters = new ArrayList<>();
        for (Parameter parameter : nullToEmpty(operation.getParameters())) {
            EndpointParameter endpointParameter = schemaMapper.toEndpointParameter(parameter, openAPI);
            switch (nullToEmpty(parameter.getIn())) {
                case "path" -> pathParameters.add(endpointParameter);
                case "header" -> headerParameters.add(endpointParameter);
                default -> queryParameters.add(endpointParameter);
            }
        }

        return new EndpointRequestSpec(
            endpointCatalog.displayGroup(entry.group()),
            entry.method(),
            entry.path(),
            new EndpointParameters(pathParameters, queryParameters, headerParameters),
            schemaMapper.toRequestBodySpec(operation.getRequestBody(), openAPI)
        );
    }

    public EndpointResponseSpec getEndpointResponseSpec(String group, String method, String path) {
        EndpointEntry entry = endpointCatalog.findEndpoint(group, method, path);
        OpenAPI openAPI = openApiProvider.getOpenApi(entry.group());
        Operation operation = openApiOperation(openAPI, entry);

        return new EndpointResponseSpec(
            endpointCatalog.displayGroup(entry.group()),
            entry.method(),
            entry.path(),
            nullToEmpty(operation.getResponses()).entrySet().stream()
                .map(response -> toEndpointResponse(response.getKey(), response.getValue(), openAPI))
                .toList()
        );
    }

    private EndpointResponse toEndpointResponse(
        String status,
        ApiResponse apiResponse,
        OpenAPI openAPI
    ) {
        String responseStatus = responseStatus(status);
        if ("204".equals(responseStatus)) {
            return new EndpointResponse(
                responseStatus,
                nullToEmpty(apiResponse.getDescription()),
                List.of(),
                null
            );
        }
        return new EndpointResponse(
            responseStatus,
            nullToEmpty(apiResponse.getDescription()),
            responseContentTypes(apiResponse.getContent(), responseStatus),
            responseSchema(apiResponse.getContent(), openAPI, responseStatus)
        );
    }

    private Operation openApiOperation(OpenAPI openAPI, EndpointEntry entry) {
        PathItem pathItem = openAPI.getPaths().get(entry.path());
        if (pathItem == null) {
            throw new EndpointSpecException("OPENAPI_PATH_NOT_FOUND", "No OpenAPI path found.");
        }
        PathItem.HttpMethod httpMethod = httpMethod(entry.method());
        if (httpMethod == null) {
            throw new EndpointSpecException("UNSUPPORTED_HTTP_METHOD", "Unsupported HTTP method.");
        }
        Operation operation = pathItem.readOperationsMap().get(httpMethod);
        if (operation == null) {
            throw new EndpointSpecException("OPENAPI_OPERATION_NOT_FOUND", "No OpenAPI operation found.");
        }
        return operation;
    }

    private ReplacedBy replacedBy(Deprecation deprecation) {
        if (!hasReplacement(deprecation)) {
            return null;
        }
        return new ReplacedBy(deprecation.replacedByMethod(), deprecation.replacedByPath());
    }

    private boolean hasReplacement(Deprecation deprecation) {
        return deprecation != null
            && (!deprecation.replacedByMethod().isBlank() || !deprecation.replacedByPath().isBlank());
    }

    private PathItem.HttpMethod httpMethod(String method) {
        try {
            return PathItem.HttpMethod.valueOf(method);
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    private List<String> responseContentTypes(Content content, String responseStatus) {
        List<String> contentTypes = schemaMapper.contentTypes(content);
        if (contentTypes.isEmpty() && isErrorStatus(responseStatus)) {
            return List.of(APPLICATION_JSON);
        }
        return contentTypes;
    }

    private EndpointSchema responseSchema(Content content, OpenAPI openAPI, String responseStatus) {
        EndpointSchema schema = schemaMapper.firstContentSchema(content, openAPI);
        if (schema == null && isErrorStatus(responseStatus)) {
            return schemaMapper.errorResponseSchema();
        }
        return schema;
    }

    private EndpointSummary toSummary(EndpointEntry entry) {
        Operation operation = entry.operation();
        return new EndpointSummary(
            endpointCatalog.displayGroup(entry.group()),
            entry.method(),
            entry.path(),
            operationId(operation),
            operation == null ? "" : nullToEmpty(operation.getSummary()),
            operation == null ? "" : nullToEmpty(operation.getDescription()),
            entry.tags(),
            entry.deprecated(),
            entry.authRequired()
        );
    }

    private String responseStatus(String status) {
        int lastSpace = status.lastIndexOf(' ');
        return lastSpace == -1 ? status : status.substring(lastSpace + 1);
    }

    private boolean isErrorStatus(String status) {
        return !status.isBlank() && status.charAt(0) != '2';
    }

    private String operationId(Operation operation) {
        return operation == null ? "" : nullToEmpty(operation.getOperationId());
    }

    private String nullToEmpty(String value) {
        return Objects.requireNonNullElse(value, "");
    }

    private <T> List<T> nullToEmpty(List<T> values) {
        return values == null ? List.of() : values;
    }

    private <K, V> Map<K, V> nullToEmpty(Map<K, V> values) {
        return values == null ? Map.of() : values;
    }
}
