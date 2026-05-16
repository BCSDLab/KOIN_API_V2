package in.koreatech.koin.global.mcp.dto;

import java.util.List;

public record EndpointResponse(
    String status,
    String description,
    List<String> contentTypes,
    EndpointSchema schema
) {
}
