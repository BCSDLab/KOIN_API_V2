package in.koreatech.koin.mcp.dto.endpoint;

import java.util.List;

public record FindEndpointsResponse(List<EndpointSummary> items) {
}
