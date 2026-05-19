package in.koreatech.koin.mcp.tool;

import java.util.Locale;
import java.util.function.Supplier;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import in.koreatech.koin.mcp.McpConstants;
import in.koreatech.koin.mcp.dto.error.McpErrorResponse;
import in.koreatech.koin.mcp.exception.EndpointSpecException;
import in.koreatech.koin.mcp.model.DeprecatedFilter;
import in.koreatech.koin.mcp.service.EndpointSpecService;

@Component
@ConditionalOnProperty(name = McpConstants.SERVER_ENABLED_PROPERTY, havingValue = "true")
public class EndpointSpecTools {

    private final EndpointSpecService endpointSpecService;

    public EndpointSpecTools(EndpointSpecService endpointSpecService) {
        this.endpointSpecService = endpointSpecService;
    }

    @Tool(description = "Find KOIN API endpoints by keyword, or list all endpoints when query is omitted. This is read-only and never sends API requests.")
    public Object find_endpoints(
        @ToolParam(description = "Optional keyword matched against path, method, group, operation id, summary, description, and tags.", required = false) String query,
        @ToolParam(description = "Optional endpoint group. Exact group names and normalized names like 'business' are supported.", required = false) String group,
        @ToolParam(description = "Deprecated endpoint filter. Use 'exclude' by default, 'include' to include deprecated endpoints, or 'only' to return deprecated endpoints only.", required = false) String deprecated
    ) {
        return handle(() -> endpointSpecService.findEndpoints(query, group, parseDeprecatedFilter(deprecated)));
    }

    @Tool(description = "Get endpoint description metadata excluding request and response body details. This is read-only and never sends API requests.")
    public Object get_endpoint_description(
        @ToolParam(description = "Optional endpoint group from find_endpoints. Required only when the same method and path exist in multiple groups.", required = false) String group,
        @ToolParam(description = "HTTP method, such as GET, POST, PUT, PATCH, or DELETE.") String method,
        @ToolParam(description = "Endpoint path, such as /v2/shops/{id}.") String path
    ) {
        return handle(() -> endpointSpecService.getEndpointDescription(group, method, path));
    }

    @Tool(description = "Get endpoint request parameters and request body schema. This is read-only and never sends API requests.")
    public Object get_endpoint_request_spec(
        @ToolParam(description = "Optional endpoint group from find_endpoints. Required only when the same method and path exist in multiple groups.", required = false) String group,
        @ToolParam(description = "HTTP method, such as GET, POST, PUT, PATCH, or DELETE.") String method,
        @ToolParam(description = "Endpoint path, such as /v2/shops/{id}.") String path
    ) {
        return handle(() -> endpointSpecService.getEndpointRequestSpec(group, method, path));
    }

    @Tool(description = "Get endpoint response status codes and response body schemas paired by status code. This is read-only and never sends API requests.")
    public Object get_endpoint_response_spec(
        @ToolParam(description = "Optional endpoint group from find_endpoints. Required only when the same method and path exist in multiple groups.", required = false) String group,
        @ToolParam(description = "HTTP method, such as GET, POST, PUT, PATCH, or DELETE.") String method,
        @ToolParam(description = "Endpoint path, such as /v2/shops/{id}.") String path
    ) {
        return handle(() -> endpointSpecService.getEndpointResponseSpec(group, method, path));
    }

    private Object handle(Supplier<?> supplier) {
        try {
            return supplier.get();
        } catch (EndpointSpecException exception) {
            return McpErrorResponse.from(exception);
        }
    }

    private DeprecatedFilter parseDeprecatedFilter(String deprecated) {
        if (deprecated == null || deprecated.isBlank()) {
            return DeprecatedFilter.EXCLUDE;
        }
        return switch (deprecated.trim().toLowerCase(Locale.ROOT)) {
            case "exclude" -> DeprecatedFilter.EXCLUDE;
            case "include" -> DeprecatedFilter.INCLUDE;
            case "only" -> DeprecatedFilter.ONLY;
            default -> throw new EndpointSpecException(
                "INVALID_DEPRECATED_FILTER",
                "deprecated must be one of exclude, include, only."
            );
        };
    }
}
