package in.koreatech.koin.global.mcp.dto;

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
    String deprecatedSince,
    String deprecatedReason,
    ReplacedBy replacedBy,
    boolean forRemoval,
    boolean authRequired
) {
}
