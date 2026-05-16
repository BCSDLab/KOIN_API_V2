package in.koreatech.koin.global.mcp.dto;

import java.util.List;

public record RequestBodySpec(
    boolean required,
    List<String> contentTypes,
    EndpointSchema schema
) {
}
