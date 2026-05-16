package in.koreatech.koin.global.mcp.dto;

public record EndpointParameter(
    String name,
    boolean required,
    String description,
    EndpointSchema schema
) {
}
