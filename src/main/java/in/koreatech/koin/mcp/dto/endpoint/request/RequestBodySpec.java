package in.koreatech.koin.mcp.dto.endpoint.request;

import java.util.List;

import in.koreatech.koin.mcp.dto.schema.EndpointSchema;

public record RequestBodySpec(
    boolean required,
    List<String> contentTypes,
    EndpointSchema schema
) {
}
