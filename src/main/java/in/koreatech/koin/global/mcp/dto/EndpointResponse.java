package in.koreatech.koin.global.mcp.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record EndpointResponse(
    String status,
    String code,
    String description,
    List<String> contentTypes,
    EndpointSchema schema
) {
}
