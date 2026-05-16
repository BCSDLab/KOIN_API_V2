package in.koreatech.koin.global.mcp.dto;

import java.util.List;

public record EndpointResponseSpec(
    String group,
    String method,
    String path,
    List<EndpointResponse> responses
) {
}
