package in.koreatech.koin.mcp.service;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import in.koreatech.koin.global.exception.ErrorResponse;
import in.koreatech.koin.mcp.McpConstants;
import in.koreatech.koin.mcp.dto.endpoint.request.EndpointParameter;
import in.koreatech.koin.mcp.dto.endpoint.request.RequestBodySpec;
import in.koreatech.koin.mcp.dto.schema.EndpointSchema;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.core.converter.ResolvedSchema;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;

@Component
@ConditionalOnProperty(name = McpConstants.SERVER_ENABLED_PROPERTY, havingValue = "true")
public class EndpointSchemaMapper {

    private static final int SCHEMA_MAX_DEPTH = 5;
    private static final String APPLICATION_JSON = "application/json";
    private static final String OBJECT_TYPE = "object";
    private static final String TRUNCATED_EXTENSION = "x-truncated";
    private static final String STRING_TYPE = "string";

    public EndpointParameter toEndpointParameter(Parameter parameter, OpenAPI openAPI) {
        Parameter resolvedParameter = resolveParameter(parameter, openAPI);
        return new EndpointParameter(
            resolvedParameter.getName(),
            Boolean.TRUE.equals(resolvedParameter.getRequired()),
            Objects.requireNonNullElse(resolvedParameter.getDescription(), ""),
            toEndpointSchema(resolveSchema(resolvedParameter.getSchema(), openAPI))
        );
    }

    public RequestBodySpec toRequestBodySpec(RequestBody requestBody, OpenAPI openAPI) {
        if (requestBody == null) {
            return null;
        }
        return new RequestBodySpec(
            Boolean.TRUE.equals(requestBody.getRequired()),
            contentTypes(requestBody.getContent()),
            firstContentSchema(requestBody.getContent(), openAPI)
        );
    }

    public List<String> contentTypes(Content content) {
        if (content == null) {
            return List.of();
        }
        return content.keySet().stream()
            .map(contentType -> "*/*".equals(contentType) ? APPLICATION_JSON : contentType)
            .toList();
    }

    public EndpointSchema firstContentSchema(Content content, OpenAPI openAPI) {
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

    public EndpointSchema errorResponseSchema() {
        ResolvedSchema resolvedSchema = ModelConverters.getInstance()
            .readAllAsResolvedSchema(ErrorResponse.class);
        if (resolvedSchema == null || resolvedSchema.schema == null) {
            return null;
        }
        return toEndpointSchema(resolveRefs(
            resolvedSchema.schema,
            resolvedSchema.referencedSchemas,
            new HashSet<>(),
            0
        ));
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

    private EndpointSchema toEndpointSchema(Schema<?> schema) {
        if (schema == null) {
            return null;
        }

        Map<String, EndpointSchema> properties = null;
        if (schema.getProperties() != null && !schema.getProperties().isEmpty()) {
            Map<String, EndpointSchema> convertedProperties = new LinkedHashMap<>();
            schema.getProperties().forEach((name, property) ->
                convertedProperties.put(name, toEndpointSchema(property)));
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
            isTruncated(schema) ? true : null
        );
    }

    private List<EndpointSchema> toEndpointSchemaList(List<?> schemas) {
        if (schemas == null || schemas.isEmpty()) {
            return Collections.emptyList();
        }
        return schemas.stream()
            .filter(Schema.class::isInstance)
            .map(Schema.class::cast)
            .map(this::toEndpointSchema)
            .toList();
    }

    private boolean isTruncated(Schema<?> schema) {
        if (schema.getExtensions() == null) {
            return false;
        }
        Object truncated = schema.getExtensions().get(TRUNCATED_EXTENSION);
        return Boolean.TRUE.equals(truncated);
    }

    private Schema<?> resolveRefs(
        Schema<?> schema,
        Map<String, ?> referencedSchemas,
        Set<String> resolvingRefs,
        int depth
    ) {
        if (schema == null) {
            return null;
        }
        if (depth > SCHEMA_MAX_DEPTH) {
            return truncatedSchema(schema);
        }

        String ref = schema.get$ref();
        if (ref != null && !ref.isBlank()) {
            return resolveRef(ref, referencedSchemas, resolvingRefs, depth);
        }

        resolveChildSchemas(schema, referencedSchemas, resolvingRefs, depth);
        return schema;
    }

    private Schema<?> resolveRef(
        String ref,
        Map<String, ?> referencedSchemas,
        Set<String> resolvingRefs,
        int depth
    ) {
        String refName = ref.substring(ref.lastIndexOf('/') + 1);
        if (referencedSchemas == null) {
            return fallbackSchema(refName);
        }
        if (!resolvingRefs.add(refName)) {
            Schema<?> truncatedSchema = fallbackSchema(refName);
            truncatedSchema.addExtension(TRUNCATED_EXTENSION, true);
            return truncatedSchema;
        }
        Object referencedSchema = referencedSchemas.get(refName);
        Schema<?> resolved = referencedSchema == null
            ? fallbackSchema(refName)
            : resolveRefs((Schema<?>)referencedSchema, referencedSchemas, resolvingRefs, depth + 1);
        resolvingRefs.remove(refName);
        return resolved;
    }

    private void resolveChildSchemas(
        Schema<?> schema,
        Map<String, ?> referencedSchemas,
        Set<String> resolvingRefs,
        int depth
    ) {
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
    }

    private Schema<?> truncatedSchema(Schema<?> schema) {
        String ref = schema.get$ref();
        if (ref != null && !ref.isBlank()) {
            Schema<?> fallback = fallbackSchema(ref.substring(ref.lastIndexOf('/') + 1));
            fallback.addExtension(TRUNCATED_EXTENSION, true);
            return fallback;
        }
        Schema<?> truncated = new Schema<>()
            .type(schema.getType() == null ? OBJECT_TYPE : schema.getType())
            .format(schema.getFormat())
            .description(schema.getDescription());
        truncated.addExtension(TRUNCATED_EXTENSION, true);
        return truncated;
    }

    private Schema<?> fallbackSchema(String refName) {
        if (refName.endsWith("LocalTime")) {
            return new Schema<>().type(STRING_TYPE).format("time");
        }
        if (refName.endsWith("LocalDate")) {
            return new Schema<>().type(STRING_TYPE).format("date");
        }
        if (refName.endsWith("LocalDateTime")
            || refName.endsWith("OffsetDateTime")
            || refName.endsWith("ZonedDateTime")) {
            return new Schema<>().type(STRING_TYPE).format("date-time");
        }
        if (refName.endsWith("UUID")) {
            return new Schema<>().type(STRING_TYPE).format("uuid");
        }
        return new Schema<>().type(OBJECT_TYPE).description("Unresolved schema: " + refName);
    }
}
