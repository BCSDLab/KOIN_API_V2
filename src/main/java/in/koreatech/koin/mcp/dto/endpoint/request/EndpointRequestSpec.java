package in.koreatech.koin.mcp.dto.endpoint.request;

public record EndpointRequestSpec(
    String group,
    String method,
    String path,
    EndpointParameters parameters,
    RequestBodySpec requestBody
) {
}
