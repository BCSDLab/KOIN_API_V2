package in.koreatech.koin.mcp.dto.error;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;

import in.koreatech.koin.mcp.dto.endpoint.EndpointCandidate;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record McpError(
    String code,
    String message,
    Map<String, String> details,
    List<EndpointCandidate> candidates
) {
}
