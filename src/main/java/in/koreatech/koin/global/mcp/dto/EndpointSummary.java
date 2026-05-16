package in.koreatech.koin.global.mcp.dto;

import java.util.List;

public record EndpointSummary(
    String group,
    String method,
    String path,
    String operationId,
    String summary,
    String description,
    List<String> tags,
    boolean deprecated,
    boolean authRequired
) {
}
