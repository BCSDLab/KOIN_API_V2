package in.koreatech.koin.global.mcp.model;

import java.lang.reflect.Method;
import java.util.List;

import in.koreatech.koin.global.code.Deprecation;
import io.swagger.v3.oas.models.Operation;

public record EndpointEntry(
    String group,
    String method,
    String path,
    Method docsMethod,
    Operation operation,
    List<String> tags,
    Deprecation deprecation,
    boolean deprecated,
    boolean authRequired
) {
}
