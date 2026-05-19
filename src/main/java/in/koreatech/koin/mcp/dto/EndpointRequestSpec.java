package in.koreatech.koin.mcp.dto;

public record EndpointRequestSpec(
    String group,
    String method,
    String path,
    EndpointParameters parameters,
    RequestBodySpec requestBody
) {
}
