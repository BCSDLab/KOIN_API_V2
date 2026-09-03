package in.koreatech.koin.mcp.dto.endpoint.request;

import java.util.List;

public record EndpointParameters(
    List<EndpointParameter> path,
    List<EndpointParameter> query,
    List<EndpointParameter> header
) {
}
