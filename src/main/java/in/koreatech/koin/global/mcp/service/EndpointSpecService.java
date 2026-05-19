package in.koreatech.koin.global.mcp.service;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import in.koreatech.koin.global.auth.Auth;
import in.koreatech.koin.global.code.Deprecation;
import in.koreatech.koin.global.mcp.dto.EndpointDescription;
import in.koreatech.koin.global.mcp.dto.EndpointParameter;
import in.koreatech.koin.global.mcp.dto.EndpointParameters;
import in.koreatech.koin.global.mcp.dto.EndpointRequestSpec;
import in.koreatech.koin.global.mcp.dto.EndpointResponse;
import in.koreatech.koin.global.mcp.dto.EndpointResponseSpec;
import in.koreatech.koin.global.mcp.dto.EndpointSchema;
import in.koreatech.koin.global.mcp.dto.EndpointSummary;
import in.koreatech.koin.global.mcp.dto.FindEndpointsResponse;
import in.koreatech.koin.global.mcp.dto.ReplacedBy;
import in.koreatech.koin.global.mcp.dto.RequestBodySpec;
import in.koreatech.koin.global.mcp.exception.EndpointSpecException;
import in.koreatech.koin.global.mcp.model.DeprecatedFilter;
import in.koreatech.koin.global.mcp.model.EndpointEntry;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;

@Service
@ConditionalOnProperty(name = "spring.ai.mcp.server.enabled", havingValue = "true")
public class EndpointSpecService {

    private static final String ROOT_PACKAGE = "in.koreatech.koin";
    private static final int SCHEMA_MAX_DEPTH = 5;

    private final RequestMappingHandlerMapping handlerMapping;
    private final List<GroupedOpenApi> groupedOpenApis;
    private final McpOpenApiProvider openApiProvider;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public EndpointSpecService(
        @Qualifier("requestMappingHandlerMapping") RequestMappingHandlerMapping handlerMapping,
        List<GroupedOpenApi> groupedOpenApis,
        McpOpenApiProvider openApiProvider
    ) {
        this.handlerMapping = handlerMapping;
        this.groupedOpenApis = groupedOpenApis;
        this.openApiProvider = openApiProvider;
    }

    public FindEndpointsResponse findEndpoints(String query, String group, DeprecatedFilter deprecated) {
        String normalizedGroup = normalize(group);
        String normalizedQuery = normalize(query);
        DeprecatedFilter filter = deprecated == null ? DeprecatedFilter.EXCLUDE : deprecated;

        List<EndpointSummary> items = endpointEntries().stream()
            .filter(entry -> normalizedGroup == null || matchesGroupFilter(entry.group(), normalizedGroup))
            .filter(entry -> matchesDeprecatedFilter(entry.deprecated(), filter))
            .filter(entry -> matchesQuery(entry, normalizedQuery))
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
        EndpointEntry entry = findEndpoint(group, method, path);
        Operation operation = entry.operation();
        Deprecation deprecation = entry.deprecation();

        return new EndpointDescription(
            entry.group(),
            entry.method(),
            entry.path(),
            operationId(operation),
            operation == null ? "" : nullToEmpty(operation.getSummary()),
            operation == null ? "" : nullToEmpty(operation.getDescription()),
            entry.tags(),
            entry.deprecated(),
            deprecation == null ? "" : deprecation.since(),
            deprecation == null ? "" : deprecation.reason(),
            deprecation == null || deprecation.replacedByMethod().isBlank() && deprecation.replacedByPath().isBlank()
                ? null
                : new ReplacedBy(deprecation.replacedByMethod(), deprecation.replacedByPath()),
            deprecation != null && deprecation.forRemoval(),
            entry.authRequired()
        );
    }

    public EndpointRequestSpec getEndpointRequestSpec(String group, String method, String path) {
        EndpointEntry entry = findEndpoint(group, method, path);
        List<EndpointParameter> pathParameters = new ArrayList<>();
        List<EndpointParameter> queryParameters = new ArrayList<>();
        List<EndpointParameter> headerParameters = new ArrayList<>();
        OpenAPI openAPI = openApiProvider.getOpenApi(entry.group());
        Operation operation = openApiOperation(openAPI, entry);

        for (Parameter parameter : nullToEmpty(operation.getParameters())) {
            EndpointParameter endpointParameter = toEndpointParameter(parameter, openAPI);
            switch (nullToEmpty(parameter.getIn())) {
                case "path" -> pathParameters.add(endpointParameter);
                case "header" -> headerParameters.add(endpointParameter);
                default -> queryParameters.add(endpointParameter);
            }
        }

        return new EndpointRequestSpec(
            entry.group(),
            entry.method(),
            entry.path(),
            new EndpointParameters(pathParameters, queryParameters, headerParameters),
            toRequestBodySpec(operation.getRequestBody(), openAPI)
        );
    }

    public EndpointResponseSpec getEndpointResponseSpec(String group, String method, String path) {
        EndpointEntry entry = findEndpoint(group, method, path);
        OpenAPI openAPI = openApiProvider.getOpenApi(entry.group());
        Operation operation = openApiOperation(openAPI, entry);

        return new EndpointResponseSpec(
            entry.group(),
            entry.method(),
            entry.path(),
            nullToEmpty(operation.getResponses()).entrySet().stream()
                .map(response -> toEndpointResponse(response.getKey(), response.getValue(), openAPI))
                .toList()
        );
    }

    private EndpointResponse toEndpointResponse(String status, ApiResponse apiResponse, OpenAPI openAPI) {
        String responseStatus = responseStatus(status, apiResponse);
        if ("204".equals(responseStatus)) {
            return new EndpointResponse(
                responseStatus,
                responseExtension(apiResponse, "x-koin-code"),
                nullToEmpty(apiResponse.getDescription()),
                List.of(),
                null
            );
        }
        return new EndpointResponse(
            responseStatus,
            responseExtension(apiResponse, "x-koin-code"),
            nullToEmpty(apiResponse.getDescription()),
            contentTypes(apiResponse.getContent()),
            firstContentSchema(apiResponse.getContent(), openAPI)
        );
    }

    private String responseStatus(String status, ApiResponse apiResponse) {
        String extensionStatus = responseExtension(apiResponse, "x-http-status");
        if (extensionStatus != null) {
            return extensionStatus;
        }
        int lastSpace = status.lastIndexOf(' ');
        return lastSpace == -1 ? status : status.substring(lastSpace + 1);
    }

    private String responseExtension(ApiResponse apiResponse, String key) {
        if (apiResponse.getExtensions() == null || !apiResponse.getExtensions().containsKey(key)) {
            return null;
        }
        return String.valueOf(apiResponse.getExtensions().get(key));
    }

    private EndpointParameter toEndpointParameter(Parameter parameter, OpenAPI openAPI) {
        Parameter resolvedParameter = resolveParameter(parameter, openAPI);
        return new EndpointParameter(
            resolvedParameter.getName(),
            Boolean.TRUE.equals(resolvedParameter.getRequired()),
            nullToEmpty(resolvedParameter.getDescription()),
            toEndpointSchema(resolveSchema(resolvedParameter.getSchema(), openAPI))
        );
    }

    private RequestBodySpec toRequestBodySpec(RequestBody requestBody, OpenAPI openAPI) {
        if (requestBody == null) {
            return null;
        }
        return new RequestBodySpec(
            Boolean.TRUE.equals(requestBody.getRequired()),
            contentTypes(requestBody.getContent()),
            firstContentSchema(requestBody.getContent(), openAPI)
        );
    }

    private List<String> contentTypes(Content content) {
        return content == null ? List.of() : List.copyOf(content.keySet());
    }

    private EndpointSchema firstContentSchema(Content content, OpenAPI openAPI) {
        if (content == null || content.isEmpty()) {
            return null;
        }
        return content.values().stream()
            .map(MediaType::getSchema)
            .filter(Objects::nonNull)
            .findFirst()
            .map(schema -> toEndpointSchema(resolveSchema(schema, openAPI)))
            .orElse(null);
    }

    private Parameter resolveParameter(Parameter parameter, OpenAPI openAPI) {
        String ref = parameter.get$ref();
        if (ref == null || ref.isBlank()
            || openAPI.getComponents() == null
            || openAPI.getComponents().getParameters() == null) {
            return parameter;
        }
        String name = ref.substring(ref.lastIndexOf('/') + 1);
        return openAPI.getComponents().getParameters().getOrDefault(name, parameter);
    }

    private Schema<?> resolveSchema(Schema<?> schema, OpenAPI openAPI) {
        if (openAPI.getComponents() == null) {
            return schema;
        }
        return resolveRefs(schema, openAPI.getComponents().getSchemas(), new HashSet<>(), 0);
    }

    private Operation openApiOperation(OpenAPI openAPI, EndpointEntry entry) {
        PathItem pathItem = openAPI.getPaths().get(entry.path());
        if (pathItem == null) {
            throw new EndpointSpecException("OPENAPI_PATH_NOT_FOUND", "No OpenAPI path found.");
        }
        Operation operation = pathItem.readOperationsMap().get(PathItem.HttpMethod.valueOf(entry.method()));
        if (operation == null) {
            throw new EndpointSpecException("OPENAPI_OPERATION_NOT_FOUND", "No OpenAPI operation found.");
        }
        return operation;
    }

    private Operation openApiOperation(String group, String method, String path) {
        OpenAPI openAPI;
        try {
            openAPI = openApiProvider.getOpenApi(group);
        } catch (EndpointSpecException ignored) {
            return null;
        }
        PathItem pathItem = openAPI.getPaths().get(path);
        if (pathItem == null) {
            return null;
        }
        return pathItem.readOperationsMap().get(PathItem.HttpMethod.valueOf(method));
    }

    private EndpointSummary toSummary(EndpointEntry entry) {
        Operation operation = entry.operation();
        return new EndpointSummary(
            entry.group(),
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

    private EndpointEntry findEndpoint(String group, String method, String path) {
        if (method == null || method.isBlank()) {
            throw new EndpointSpecException("METHOD_REQUIRED", "method is required.");
        }
        if (path == null || path.isBlank()) {
            throw new EndpointSpecException("PATH_REQUIRED", "path is required.");
        }
        String normalizedGroup = normalize(group);
        String normalizedMethod = method.toUpperCase(Locale.ROOT);

        List<EndpointEntry> matches = endpointEntries().stream()
            .filter(entry -> normalizedGroup == null || matchesGroupFilter(entry.group(), normalizedGroup))
            .filter(entry -> entry.method().equals(normalizedMethod))
            .filter(entry -> entry.path().equals(path))
            .toList();

        if (matches.isEmpty()) {
            throw new EndpointSpecException("ENDPOINT_NOT_FOUND", "No endpoint found.");
        }
        if (matches.size() > 1) {
            throw new EndpointSpecException("AMBIGUOUS_ENDPOINT", "Multiple endpoints found. Please specify group.");
        }
        return matches.get(0);
    }

    private boolean matchesQuery(EndpointEntry entry, String query) {
        if (query == null) {
            return true;
        }
        Operation operation = entry.operation();
        String haystack = String.join(" ",
            entry.path(),
            entry.method(),
            entry.group(),
            operationId(operation),
            operation == null ? "" : nullToEmpty(operation.getSummary()),
            operation == null ? "" : nullToEmpty(operation.getDescription()),
            String.join(" ", entry.tags())
        ).toLowerCase(Locale.ROOT);
        return haystack.contains(query.toLowerCase(Locale.ROOT));
    }

    private boolean matchesDeprecatedFilter(boolean deprecated, DeprecatedFilter filter) {
        return switch (filter) {
            case EXCLUDE -> !deprecated;
            case INCLUDE -> true;
            case ONLY -> deprecated;
        };
    }

    private List<EndpointEntry> endpointEntries() {
        List<EndpointEntry> entries = new ArrayList<>();
        handlerMapping.getHandlerMethods().forEach((info, handlerMethod) -> {
            if (!handlerMethod.getBeanType().getPackageName().startsWith(ROOT_PACKAGE)) {
                return;
            }
            Method docsMethod = findDocsMethod(handlerMethod);
            Deprecation deprecation = findDeprecation(docsMethod);

            for (String path : paths(info)) {
                for (String group : groupsOf(handlerMethod.getBeanType(), path)) {
                    for (String method : methods(info)) {
                        Operation operation = openApiOperation(group, method, path);
                        entries.add(new EndpointEntry(
                            group,
                            method,
                            path,
                            docsMethod,
                            operation,
                            operationTags(operation, handlerMethod.getBeanType(), docsMethod),
                            deprecation,
                            operation != null && Boolean.TRUE.equals(operation.getDeprecated()) || deprecation != null,
                            authRequired(docsMethod, operation)
                        ));
                    }
                }
            }
        });
        return entries;
    }

    private Method findDocsMethod(HandlerMethod handlerMethod) {
        Method controllerMethod = handlerMethod.getMethod();
        for (Class<?> apiInterface : handlerMethod.getBeanType().getInterfaces()) {
            for (Method interfaceMethod : apiInterface.getMethods()) {
                if (hasSameSignature(interfaceMethod, controllerMethod)) {
                    return interfaceMethod;
                }
            }
        }
        return controllerMethod;
    }

    private Deprecation findDeprecation(Method method) {
        return AnnotatedElementUtils.findMergedAnnotation(method, Deprecation.class);
    }

    private List<String> operationTags(Operation operation, Class<?> beanType, Method method) {
        return operation != null && operation.getTags() != null
            ? operation.getTags()
            : findTags(beanType, method);
    }

    private List<String> findTags(Class<?> beanType, Method method) {
        Set<String> tags = new LinkedHashSet<>();
        Tag methodTag = AnnotatedElementUtils.findMergedAnnotation(method, Tag.class);
        if (methodTag != null && !methodTag.name().isBlank()) {
            tags.add(methodTag.name());
        }
        for (Class<?> apiInterface : beanType.getInterfaces()) {
            Tag interfaceTag = AnnotatedElementUtils.findMergedAnnotation(apiInterface, Tag.class);
            if (interfaceTag != null && !interfaceTag.name().isBlank()) {
                tags.add(interfaceTag.name());
            }
        }
        Tag classTag = AnnotatedElementUtils.findMergedAnnotation(beanType, Tag.class);
        if (classTag != null && !classTag.name().isBlank()) {
            tags.add(classTag.name());
        }
        return List.copyOf(tags);
    }

    private boolean authRequired(Method method, Operation operation) {
        boolean hasAuthParameter = Arrays.stream(method.getParameters())
            .anyMatch(parameter -> parameter.isAnnotationPresent(Auth.class));
        if (hasAuthParameter) {
            return true;
        }
        if (operation != null && operation.getSecurity() != null && !operation.getSecurity().isEmpty()) {
            return true;
        }
        return method.isAnnotationPresent(SecurityRequirement.class);
    }

    private List<String> paths(RequestMappingInfo info) {
        if (info.getPathPatternsCondition() != null) {
            return info.getPathPatternsCondition().getPatterns().stream()
                .map(pattern -> pattern.getPatternString())
                .toList();
        }
        if (info.getPatternsCondition() != null) {
            return List.copyOf(info.getPatternsCondition().getPatterns());
        }
        return List.of();
    }

    private List<String> methods(RequestMappingInfo info) {
        Set<RequestMethod> methods = info.getMethodsCondition().getMethods();
        if (methods.isEmpty()) {
            return Arrays.stream(RequestMethod.values()).map(Enum::name).toList();
        }
        return methods.stream().map(Enum::name).toList();
    }

    private List<String> groupsOf(Class<?> beanType, String path) {
        String packageName = beanType.getPackageName();
        List<String> matchedGroups = groupedOpenApis.stream()
            .filter(groupedOpenApi -> matchesGroupedOpenApi(groupedOpenApi, packageName, path))
            .map(GroupedOpenApi::getGroup)
            .toList();

        return matchedGroups.isEmpty() ? List.of("unknown") : matchedGroups;
    }

    private boolean matchesGroupedOpenApi(GroupedOpenApi groupedOpenApi, String packageName, String path) {
        if (matchesAny(path, groupedOpenApi.getPathsToExclude())) {
            return false;
        }
        if (startsWithAny(packageName, groupedOpenApi.getPackagesToExclude())) {
            return false;
        }

        boolean pathMatched = isEmpty(groupedOpenApi.getPathsToMatch())
            || matchesAny(path, groupedOpenApi.getPathsToMatch());
        boolean packageMatched = isEmpty(groupedOpenApi.getPackagesToScan())
            || startsWithAny(packageName, groupedOpenApi.getPackagesToScan());

        return pathMatched && packageMatched;
    }

    private boolean isEmpty(List<String> values) {
        return values == null || values.isEmpty();
    }

    private boolean matchesAny(String path, List<String> patterns) {
        if (patterns == null || patterns.isEmpty()) {
            return false;
        }
        return patterns.stream().anyMatch(pattern -> pathMatcher.match(pattern, path));
    }

    private boolean startsWithAny(String packageName, List<String> packagePrefixes) {
        if (packagePrefixes == null || packagePrefixes.isEmpty()) {
            return false;
        }
        return packagePrefixes.stream().anyMatch(packageName::startsWith);
    }

    private boolean matchesGroupFilter(String actualGroup, String requestedGroup) {
        return normalizeGroup(actualGroup).equals(requestedGroup)
            || actualGroup.equalsIgnoreCase(requestedGroup);
    }

    private EndpointSchema toEndpointSchema(Schema<?> schema) {
        if (schema == null) {
            return null;
        }

        Map<String, EndpointSchema> properties = null;
        if (schema.getProperties() != null && !schema.getProperties().isEmpty()) {
            Map<String, EndpointSchema> convertedProperties = new LinkedHashMap<>();
            schema.getProperties().forEach((name, property) ->
                convertedProperties.put(name, toEndpointSchema((Schema<?>)property)));
            properties = convertedProperties;
        }

        EndpointSchema additionalProperties = null;
        if (schema.getAdditionalProperties() instanceof Schema<?> additionalPropertySchema) {
            additionalProperties = toEndpointSchema(additionalPropertySchema);
        }

        return new EndpointSchema(
            schema.getType(),
            schema.getFormat(),
            schema.getDescription(),
            schema.getExample(),
            schema.getNullable(),
            schema.getDeprecated(),
            schema.getRequired(),
            schema.getEnum(),
            properties,
            toEndpointSchema(schema.getItems()),
            additionalProperties,
            toEndpointSchemaList(schema.getAllOf()),
            toEndpointSchemaList(schema.getOneOf()),
            toEndpointSchemaList(schema.getAnyOf()),
            schema.get$ref(),
            isTruncated(schema)
        );
    }

    private List<EndpointSchema> toEndpointSchemaList(List<Schema> schemas) {
        if (schemas == null || schemas.isEmpty()) {
            return Collections.emptyList();
        }
        return schemas.stream()
            .map(this::toEndpointSchema)
            .toList();
    }

    private Boolean isTruncated(Schema<?> schema) {
        if (schema.getExtensions() == null) {
            return null;
        }
        Object truncated = schema.getExtensions().get("x-truncated");
        return Boolean.TRUE.equals(truncated) ? true : null;
    }

    @SuppressWarnings({"rawtypes"})
    private Schema<?> resolveRefs(
        Schema<?> schema,
        Map<String, Schema> referencedSchemas,
        Set<String> resolvingRefs,
        int depth
    ) {
        if (schema == null || depth > SCHEMA_MAX_DEPTH) {
            return schema;
        }

        String ref = schema.get$ref();
        if (ref != null && !ref.isBlank() && referencedSchemas != null) {
            String refName = ref.substring(ref.lastIndexOf('/') + 1);
            if (!resolvingRefs.add(refName)) {
                Schema<?> truncatedSchema = new Schema<>().$ref(ref);
                truncatedSchema.addExtension("x-truncated", true);
                return truncatedSchema;
            }
            Schema<?> referencedSchema = referencedSchemas.get(refName);
            Schema<?> resolved = referencedSchema == null
                ? schema
                : resolveRefs(referencedSchema, referencedSchemas, resolvingRefs, depth + 1);
            resolvingRefs.remove(refName);
            return resolved;
        }

        if (schema.getProperties() != null) {
            schema.getProperties().replaceAll((name, property) ->
                resolveRefs(property, referencedSchemas, resolvingRefs, depth + 1));
        }
        if (schema.getItems() != null) {
            schema.setItems(resolveRefs(schema.getItems(), referencedSchemas, resolvingRefs, depth + 1));
        }
        if (schema.getAdditionalProperties() instanceof Schema<?> additionalProperties) {
            schema.setAdditionalProperties(
                resolveRefs(additionalProperties, referencedSchemas, resolvingRefs, depth + 1));
        }
        schema.setAllOf(resolveSchemaList(schema.getAllOf(), referencedSchemas, resolvingRefs, depth));
        schema.setOneOf(resolveSchemaList(schema.getOneOf(), referencedSchemas, resolvingRefs, depth));
        schema.setAnyOf(resolveSchemaList(schema.getAnyOf(), referencedSchemas, resolvingRefs, depth));
        return schema;
    }

    private List<Schema> resolveSchemaList(
        List<Schema> schemas,
        Map<String, Schema> referencedSchemas,
        Set<String> resolvingRefs,
        int depth
    ) {
        if (schemas == null) {
            return null;
        }
        return schemas.stream()
            .map(schema -> (Schema)resolveRefs(schema, referencedSchemas, resolvingRefs, depth + 1))
            .toList();
    }

    private String operationId(Operation operation) {
        return operation == null ? "" : nullToEmpty(operation.getOperationId());
    }

    private boolean hasSameSignature(Method left, Method right) {
        return left.getName().equals(right.getName())
            && Arrays.equals(left.getParameterTypes(), right.getParameterTypes());
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeGroup(String value) {
        String normalized = normalize(value);
        if (normalized == null) {
            return "";
        }
        return normalized
            .replaceFirst("^\\d+\\.\\s*", "")
            .replaceFirst("\\s+api$", "")
            .replaceAll("[^a-z0-9]+", "-")
            .replaceAll("(^-)|(-$)", "");
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
