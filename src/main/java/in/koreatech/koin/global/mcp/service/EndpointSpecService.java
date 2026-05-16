package in.koreatech.koin.global.mcp.service;

import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
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
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ValueConstants;
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
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.core.converter.ResolvedSchema;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.models.media.Schema;

@Service
@ConditionalOnProperty(name = "spring.ai.mcp.server.enabled", havingValue = "true")
public class EndpointSpecService {

    private static final String ROOT_PACKAGE = "in.koreatech.koin";
    private static final int SCHEMA_MAX_DEPTH = 5;

    private final RequestMappingHandlerMapping handlerMapping;
    private final List<GroupedOpenApi> groupedOpenApis;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public EndpointSpecService(
        @Qualifier("requestMappingHandlerMapping") RequestMappingHandlerMapping handlerMapping,
        List<GroupedOpenApi> groupedOpenApis
    ) {
        this.handlerMapping = handlerMapping;
        this.groupedOpenApis = groupedOpenApis;
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
            operation == null ? "" : nullToEmpty(operation.summary()),
            operation == null ? "" : nullToEmpty(operation.description()),
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
        Method docsMethod = entry.docsMethod();

        List<EndpointParameter> pathParameters = new ArrayList<>();
        List<EndpointParameter> queryParameters = new ArrayList<>();
        List<EndpointParameter> headerParameters = new ArrayList<>();
        RequestBodySpec requestBody = null;

        java.lang.reflect.Parameter[] parameters = docsMethod.getParameters();
        for (java.lang.reflect.Parameter parameter : parameters) {
            if (parameter.isAnnotationPresent(Auth.class)) {
                continue;
            }
            PathVariable pathVariable = parameter.getAnnotation(PathVariable.class);
            if (pathVariable != null) {
                pathParameters.add(toParameter(parameter, parameterName(pathVariable.name(), pathVariable.value(), parameter), true));
                continue;
            }

            RequestParam requestParam = parameter.getAnnotation(RequestParam.class);
            if (requestParam != null) {
                boolean required = requestParam.required() && ValueConstants.DEFAULT_NONE.equals(requestParam.defaultValue());
                queryParameters.add(toParameter(parameter, parameterName(requestParam.name(), requestParam.value(), parameter), required));
                continue;
            }

            RequestHeader requestHeader = parameter.getAnnotation(RequestHeader.class);
            if (requestHeader != null) {
                boolean required = requestHeader.required() && ValueConstants.DEFAULT_NONE.equals(requestHeader.defaultValue());
                headerParameters.add(toParameter(parameter, parameterName(requestHeader.name(), requestHeader.value(), parameter), required));
                continue;
            }

            RequestBody body = parameter.getAnnotation(RequestBody.class);
            if (body != null) {
                requestBody = new RequestBodySpec(
                    body.required(),
                    List.of(APPLICATION_JSON_VALUE),
                    loadEndpointSchema(parameter.getParameterizedType())
                );
            }
        }

        return new EndpointRequestSpec(
            entry.group(),
            entry.method(),
            entry.path(),
            new EndpointParameters(pathParameters, queryParameters, headerParameters),
            requestBody
        );
    }

    public EndpointResponseSpec getEndpointResponseSpec(String group, String method, String path) {
        EndpointEntry entry = findEndpoint(group, method, path);
        ApiResponses apiResponses = entry.docsMethod().getAnnotation(ApiResponses.class);
        List<EndpointResponse> responses = new ArrayList<>();

        if (apiResponses != null) {
            for (ApiResponse apiResponse : apiResponses.value()) {
                responses.add(toEndpointResponse(apiResponse, entry.returnType()));
            }
        }

        if (responses.isEmpty()) {
            responses.add(defaultSuccessResponse(entry.returnType()));
        }

        return new EndpointResponseSpec(
            entry.group(),
            entry.method(),
            entry.path(),
            responses
        );
    }

    private EndpointResponse toEndpointResponse(ApiResponse apiResponse, Type returnType) {
        EndpointSchema schema = null;
        List<String> contentTypes = new ArrayList<>();

        for (Content content : apiResponse.content()) {
            if (content.schema().hidden()) {
                continue;
            }
            String mediaType = content.mediaType().isBlank() ? APPLICATION_JSON_VALUE : content.mediaType();
            contentTypes.add(mediaType);
            if (!Void.class.equals(content.schema().implementation())) {
                schema = loadEndpointSchema(content.schema().implementation());
            }
        }

        if (schema == null && isSuccess(apiResponse.responseCode())) {
            schema = loadEndpointSchema(returnType);
            if (schema != null && contentTypes.isEmpty()) {
                contentTypes.add(APPLICATION_JSON_VALUE);
            }
        }

        return new EndpointResponse(
            apiResponse.responseCode(),
            nullToEmpty(apiResponse.description()),
            contentTypes,
            schema
        );
    }

    private EndpointResponse defaultSuccessResponse(Type returnType) {
        return new EndpointResponse(
            Void.class.equals(returnType) || void.class.equals(returnType) ? "204" : "200",
            "",
            Void.class.equals(returnType) || void.class.equals(returnType) ? List.of() : List.of(APPLICATION_JSON_VALUE),
            loadEndpointSchema(returnType)
        );
    }

    private EndpointParameter toParameter(java.lang.reflect.Parameter parameter, String name, boolean required) {
        Parameter swaggerParameter = parameter.getAnnotation(Parameter.class);
        return new EndpointParameter(
            name,
            required,
            swaggerParameter == null ? "" : nullToEmpty(swaggerParameter.description()),
            loadEndpointSchema(parameter.getParameterizedType())
        );
    }

    private EndpointSummary toSummary(EndpointEntry entry) {
        Operation operation = entry.operation();
        return new EndpointSummary(
            entry.group(),
            entry.method(),
            entry.path(),
            operationId(operation),
            operation == null ? "" : nullToEmpty(operation.summary()),
            operation == null ? "" : nullToEmpty(operation.description()),
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
            operation == null ? "" : nullToEmpty(operation.summary()),
            operation == null ? "" : nullToEmpty(operation.description()),
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
            Operation operation = findOperation(docsMethod);
            Deprecation deprecation = findDeprecation(docsMethod);
            List<String> tags = findTags(handlerMethod.getBeanType(), docsMethod);

            for (String path : paths(info)) {
                for (String group : groupsOf(handlerMethod.getBeanType(), path)) {
                    for (String method : methods(info)) {
                        entries.add(new EndpointEntry(
                            group,
                            method,
                            path,
                            docsMethod,
                            operation,
                            tags,
                            deprecation,
                            operation != null && operation.deprecated() || deprecation != null,
                            authRequired(docsMethod, operation),
                            actualReturnType(docsMethod)
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

    private Operation findOperation(Method method) {
        return AnnotatedElementUtils.findMergedAnnotation(method, Operation.class);
    }

    private Deprecation findDeprecation(Method method) {
        return AnnotatedElementUtils.findMergedAnnotation(method, Deprecation.class);
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
        if (operation != null && operation.security().length > 0) {
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

    private Type actualReturnType(Method method) {
        Type returnType = method.getGenericReturnType();
        if (returnType instanceof ParameterizedType parameterizedType
            && parameterizedType.getRawType().equals(ResponseEntity.class)) {
            return parameterizedType.getActualTypeArguments()[0];
        }
        return returnType;
    }

    private Schema<?> loadSchema(Type type) {
        if (type.equals(Void.class) || type.equals(void.class)) {
            return null;
        }
        ResolvedSchema resolvedSchema = ModelConverters.getInstance().readAllAsResolvedSchema(type);
        if (resolvedSchema == null || resolvedSchema.schema == null) {
            return scalarSchema(type);
        }
        return resolveRefs(resolvedSchema.schema, resolvedSchema.referencedSchemas, new HashSet<>(), 0);
    }

    private Schema<?> scalarSchema(Type type) {
        if (!(type instanceof Class<?> clazz)) {
            return null;
        }
        if (String.class.equals(clazz) || Character.class.equals(clazz) || char.class.equals(clazz)) {
            return new Schema<>().type("string");
        }
        if (Boolean.class.equals(clazz) || boolean.class.equals(clazz)) {
            return new Schema<>().type("boolean");
        }
        if (Integer.class.equals(clazz) || int.class.equals(clazz)) {
            return new Schema<>().type("integer").format("int32");
        }
        if (Long.class.equals(clazz) || long.class.equals(clazz)) {
            return new Schema<>().type("integer").format("int64");
        }
        if (Float.class.equals(clazz) || float.class.equals(clazz)) {
            return new Schema<>().type("number").format("float");
        }
        if (Double.class.equals(clazz) || double.class.equals(clazz)) {
            return new Schema<>().type("number").format("double");
        }
        return null;
    }

    private EndpointSchema loadEndpointSchema(Type type) {
        return toEndpointSchema(loadSchema(type));
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
            return null;
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

    @SuppressWarnings({"rawtypes", "unchecked"})
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
        if (ref != null && !ref.isBlank()) {
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
                resolveRefs((Schema<?>)property, referencedSchemas, resolvingRefs, depth + 1));
        }
        if (schema.getItems() != null) {
            schema.setItems(resolveRefs(schema.getItems(), referencedSchemas, resolvingRefs, depth + 1));
        }
        if (schema.getAdditionalProperties() instanceof Schema<?> additionalProperties) {
            schema.setAdditionalProperties(resolveRefs(additionalProperties, referencedSchemas, resolvingRefs, depth + 1));
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

    private String parameterName(String name, String value, java.lang.reflect.Parameter parameter) {
        if (!name.isBlank()) {
            return name;
        }
        if (!value.isBlank()) {
            return value;
        }
        return parameter.getName();
    }

    private String operationId(Operation operation) {
        return operation == null ? "" : nullToEmpty(operation.operationId());
    }

    private boolean isSuccess(String status) {
        return status != null && !status.isBlank() && status.charAt(0) == '2';
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
        return normalize(value)
            .replaceFirst("^\\d+\\.\\s*", "")
            .replaceFirst("\\s+api$", "")
            .replaceAll("[^a-z0-9]+", "-")
            .replaceAll("(^-|-$)", "");
    }

    private String nullToEmpty(String value) {
        return Objects.requireNonNullElse(value, "");
    }
}
