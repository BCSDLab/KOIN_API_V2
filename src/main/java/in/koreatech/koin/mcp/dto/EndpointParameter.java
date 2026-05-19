package in.koreatech.koin.mcp.dto;

public record EndpointParameter(
    String name,
    boolean required,
    String description,
    EndpointSchema schema
) {
}
