package in.koreatech.koin.mcp.dto.endpoint;

import java.util.List;

public record EndpointDescription(
    String group,
    String method,
    String path,
    String operationId,
    String summary,
    String description,
    List<String> tags,
    boolean deprecated,
    String deprecatedReason,
    ReplacedBy replacedBy,
    boolean authRequired
) {
}
