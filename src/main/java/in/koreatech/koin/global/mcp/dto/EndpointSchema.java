package in.koreatech.koin.global.mcp.dto;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record EndpointSchema(
    String type,
    String format,
    String description,
    Object example,
    Boolean nullable,
    Boolean deprecated,
    List<String> required,
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
}
