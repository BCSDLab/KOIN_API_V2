package in.koreatech.koin.global.mcp.dto;

public record EndpointRequestSpec(
    String group,
    String method,
    String path,
    EndpointParameters parameters,
    RequestBodySpec requestBody
) {
}
