package in.koreatech.koin.mcp.service;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
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
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import in.koreatech.koin.global.auth.Auth;
import in.koreatech.koin.global.code.Deprecation;
import in.koreatech.koin.mcp.McpConstants;
import in.koreatech.koin.mcp.dto.endpoint.EndpointCandidate;
import in.koreatech.koin.mcp.exception.EndpointSpecException;
import in.koreatech.koin.mcp.model.DeprecatedFilter;
import in.koreatech.koin.mcp.model.EndpointEntry;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;

@Component
@ConditionalOnProperty(name = McpConstants.SERVER_ENABLED_PROPERTY, havingValue = "true")
public class EndpointCatalog {

    private static final String ROOT_PACKAGE = "in.koreatech.koin";

    private final RequestMappingHandlerMapping handlerMapping;
    private final List<GroupedOpenApi> groupedOpenApis;
    private final McpOpenApiProvider openApiProvider;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public EndpointCatalog(
        @Qualifier("requestMappingHandlerMapping") RequestMappingHandlerMapping handlerMapping,
        List<GroupedOpenApi> groupedOpenApis,
        McpOpenApiProvider openApiProvider
    ) {
        this.handlerMapping = handlerMapping;
        this.groupedOpenApis = groupedOpenApis;
        this.openApiProvider = openApiProvider;
    }

    public List<EndpointEntry> findAll(String query, String group, DeprecatedFilter deprecated) {
        String normalizedGroup = normalize(group);
        String normalizedQuery = normalize(query);
        DeprecatedFilter filter = deprecated == null ? DeprecatedFilter.EXCLUDE : deprecated;

        return entries().stream()
            .filter(entry -> normalizedGroup == null || matchesGroupFilter(entry.group(), normalizedGroup))
            .filter(entry -> matchesDeprecatedFilter(entry.deprecated(), filter))
            .filter(entry -> matchesQuery(entry, normalizedQuery))
            .toList();
    }

    public EndpointEntry findEndpoint(String group, String method, String path) {
        if (method == null || method.isBlank()) {
            throw new EndpointSpecException("METHOD_REQUIRED", "method is required.");
        }
        if (path == null || path.isBlank()) {
            throw new EndpointSpecException("PATH_REQUIRED", "path is required.");
        }
        String normalizedGroup = normalize(group);
        String normalizedMethod = method.toUpperCase(Locale.ROOT);

        List<EndpointEntry> matches = entries().stream()
            .filter(entry -> normalizedGroup == null || matchesGroupFilter(entry.group(), normalizedGroup))
            .filter(entry -> entry.method().equals(normalizedMethod))
            .filter(entry -> entry.path().equals(path))
            .toList();

        if (matches.isEmpty()) {
            throw new EndpointSpecException(
                "ENDPOINT_NOT_FOUND",
                "No endpoint found.",
                endpointDetails(group, normalizedMethod, path)
            );
        }
        if (matches.size() > 1) {
            throw new EndpointSpecException(
                "AMBIGUOUS_ENDPOINT",
                "Multiple endpoints found. Please specify group.",
                Map.of(),
                matches.stream()
                    .map(this::toCandidate)
                    .toList()
            );
        }
        return matches.get(0);
    }

    public String displayGroup(String group) {
        return normalizeGroup(group);
    }

    private List<EndpointEntry> entries() {
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

    private Map<String, String> endpointDetails(String group, String method, String path) {
        Map<String, String> details = new LinkedHashMap<>();
        if (group != null && !group.isBlank()) {
            details.put("group", displayGroup(group));
        }
        details.put("method", method);
        details.put("path", path);
        return details;
    }

    private EndpointCandidate toCandidate(EndpointEntry entry) {
        return new EndpointCandidate(displayGroup(entry.group()), entry.method(), entry.path());
    }

    private boolean hasSameSignature(Method left, Method right) {
        return left.getName().equals(right.getName())
            && Arrays.equals(left.getParameterTypes(), right.getParameterTypes());
    }

    private boolean isEmpty(List<String> values) {
        return values == null || values.isEmpty();
    }

    private String operationId(Operation operation) {
        return operation == null ? "" : nullToEmpty(operation.getOperationId());
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

    private String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim().toLowerCase(Locale.ROOT);
    }

    private String nullToEmpty(String value) {
        return Objects.requireNonNullElse(value, "");
    }
}
