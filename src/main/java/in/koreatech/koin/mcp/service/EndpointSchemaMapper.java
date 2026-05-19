package in.koreatech.koin.mcp.service;

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
    private static final String STRING_TYPE = "string";

    public EndpointParameter toEndpointParameter(Parameter parameter, OpenAPI openAPI) {
        Parameter resolvedParameter = resolveParameter(parameter, openAPI);
        return new EndpointParameter(
            resolvedParameter.getName(),
            Boolean.TRUE.equals(resolvedParameter.getRequired()),
            Objects.requireNonNullElse(resolvedParameter.getDescription(), ""),
            toEndpointSchema(resolvedParameter.getSchema(), componentSchemas(openAPI), new HashSet<>(), 0)
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
            .map(schema -> toEndpointSchema(schema, componentSchemas(openAPI), new HashSet<>(), 0))
            .orElse(null);
    }

    public EndpointSchema errorResponseSchema() {
        ResolvedSchema resolvedSchema = ModelConverters.getInstance()
            .readAllAsResolvedSchema(ErrorResponse.class);
        if (resolvedSchema == null || resolvedSchema.schema == null) {
            return null;
        }
        return toEndpointSchema(
            resolvedSchema.schema,
            resolvedSchema.referencedSchemas,
            new HashSet<>(),
            0
        );
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

    private Map<String, ?> componentSchemas(OpenAPI openAPI) {
        if (openAPI.getComponents() == null || openAPI.getComponents().getSchemas() == null) {
            return Map.of();
        }
        return openAPI.getComponents().getSchemas();
    }

    private EndpointSchema toEndpointSchema(
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

        Map<String, EndpointSchema> properties = null;
        if (schema.getProperties() != null && !schema.getProperties().isEmpty()) {
            Map<String, EndpointSchema> convertedProperties = new LinkedHashMap<>();
            schema.getProperties().forEach((name, property) ->
                convertedProperties.put(
                    name,
                    toEndpointSchema(property, referencedSchemas, resolvingRefs, depth + 1)
                ));
            properties = convertedProperties;
        }

        EndpointSchema additionalProperties = null;
        if (schema.getAdditionalProperties() instanceof Schema<?> additionalPropertySchema) {
            additionalProperties = toEndpointSchema(
                additionalPropertySchema,
                referencedSchemas,
                resolvingRefs,
                depth + 1
            );
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
            toEndpointSchema(schema.getItems(), referencedSchemas, resolvingRefs, depth + 1),
            additionalProperties,
            toEndpointSchemaList(schema.getAllOf(), referencedSchemas, resolvingRefs, depth + 1),
            toEndpointSchemaList(schema.getOneOf(), referencedSchemas, resolvingRefs, depth + 1),
            toEndpointSchemaList(schema.getAnyOf(), referencedSchemas, resolvingRefs, depth + 1),
            schema.get$ref(),
            null
        );
    }

    private List<EndpointSchema> toEndpointSchemaList(
        List<?> schemas,
        Map<String, ?> referencedSchemas,
        Set<String> resolvingRefs,
        int depth
    ) {
        if (schemas == null || schemas.isEmpty()) {
            return List.of();
        }
        return schemas.stream()
            .filter(Schema.class::isInstance)
            .map(Schema.class::cast)
            .map(schema -> toEndpointSchema(schema, referencedSchemas, resolvingRefs, depth))
            .toList();
    }

    private EndpointSchema resolveRef(
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
            return truncatedSchema(fallbackSchema(refName));
        }
        Object referencedSchema = referencedSchemas.get(refName);
        try {
            if (referencedSchema instanceof Schema<?> schema) {
                return toEndpointSchema(schema, referencedSchemas, resolvingRefs, depth + 1);
            }
            return fallbackSchema(refName);
        } finally {
            resolvingRefs.remove(refName);
        }
    }

    private EndpointSchema truncatedSchema(Schema<?> schema) {
        String ref = schema.get$ref();
        if (ref != null && !ref.isBlank()) {
            return truncatedSchema(fallbackSchema(ref.substring(ref.lastIndexOf('/') + 1)));
        }
        return new EndpointSchema(
            schema.getType() == null ? OBJECT_TYPE : schema.getType(),
            schema.getFormat(),
            schema.getDescription(),
            schema.getExample(),
            schema.getNullable(),
            schema.getDeprecated(),
            schema.getRequired(),
            schema.getEnum(),
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            true
        );
    }

    private EndpointSchema truncatedSchema(EndpointSchema schema) {
        return new EndpointSchema(
            schema.type(),
            schema.format(),
            schema.description(),
            schema.example(),
            schema.nullable(),
            schema.deprecated(),
            schema.required(),
            schema.enumValues(),
            schema.properties(),
            schema.items(),
            schema.additionalProperties(),
            schema.allOf(),
            schema.oneOf(),
            schema.anyOf(),
            schema.ref(),
            true
        );
    }

    private EndpointSchema fallbackSchema(String refName) {
        if (refName.endsWith("LocalTime")) {
            return scalarSchema("time");
        }
        if (refName.endsWith("LocalDate")) {
            return scalarSchema("date");
        }
        if (refName.endsWith("LocalDateTime")
            || refName.endsWith("OffsetDateTime")
            || refName.endsWith("ZonedDateTime")) {
            return scalarSchema("date-time");
        }
        if (refName.endsWith("UUID")) {
            return scalarSchema("uuid");
        }
        return new EndpointSchema(
            OBJECT_TYPE,
            null,
            "Unresolved schema: " + refName,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null
        );
    }

    private EndpointSchema scalarSchema(String format) {
        return new EndpointSchema(
            STRING_TYPE,
            format,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null
        );
    }
}
