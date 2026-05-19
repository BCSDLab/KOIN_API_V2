package in.koreatech.koin.mcp.dto.endpoint.response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import in.koreatech.koin.mcp.dto.schema.EndpointSchema;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record EndpointResponse(
    String status,
    String description,
    List<String> contentTypes,
    EndpointSchema schema
) {
}
