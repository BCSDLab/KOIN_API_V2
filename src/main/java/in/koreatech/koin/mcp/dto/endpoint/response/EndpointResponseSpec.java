package in.koreatech.koin.mcp.dto.endpoint.response;

import java.util.List;

public record EndpointResponseSpec(
    String group,
    String method,
    String path,
    List<EndpointResponse> responses
) {
}
