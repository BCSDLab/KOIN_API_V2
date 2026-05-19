package in.koreatech.koin.global.mcp.dto;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;

@Builder
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record EndpointSchema(
    String type,
    String format,
    String description,
    Object example,
    Boolean nullable,
    Boolean deprecated,
    List<String> required,
    @JsonProperty("enum")
    List<?> enumValues,
    Map<String, EndpointSchema> properties,
    EndpointSchema items,
    EndpointSchema additionalProperties,
    List<EndpointSchema> allOf,
    List<EndpointSchema> oneOf,
    List<EndpointSchema> anyOf,
    String ref,
    Boolean truncated
) {

    public static EndpointSchema object(Map<String, EndpointSchema> properties, List<String> required) {
        return EndpointSchema.builder()
            .type("object")
            .properties(properties)
            .required(required == null || required.isEmpty() ? null : List.copyOf(required))
            .build();
    }

    public static EndpointSchema array(EndpointSchema items) {
        return EndpointSchema.builder()
            .type("array")
            .items(items)
            .build();
    }

    public static EndpointSchema file() {
        return EndpointSchema.builder()
            .type("string")
            .format("binary")
            .build();
    }
}
