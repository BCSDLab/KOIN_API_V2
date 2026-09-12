package in.koreatech.koin.mcp.dto.endpoint.request;

import in.koreatech.koin.mcp.dto.schema.EndpointSchema;

public record EndpointParameter(
    String name,
    boolean required,
    String description,
    EndpointSchema schema
) {
}
