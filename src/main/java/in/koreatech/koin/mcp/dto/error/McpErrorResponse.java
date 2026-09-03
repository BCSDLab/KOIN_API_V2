package in.koreatech.koin.mcp.dto.error;

import in.koreatech.koin.mcp.exception.EndpointSpecException;

public record McpErrorResponse(McpError error) {

    public static McpErrorResponse from(EndpointSpecException exception) {
        return new McpErrorResponse(new McpError(
            exception.getCode(),
            exception.getMessage(),
            exception.getDetails(),
            exception.getCandidates()
        ));
    }
}
