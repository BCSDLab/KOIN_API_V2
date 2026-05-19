package in.koreatech.koin.mcp.exception;

import java.util.List;
import java.util.Map;

import in.koreatech.koin.mcp.dto.endpoint.EndpointCandidate;
import lombok.Getter;

@Getter
public class EndpointSpecException extends RuntimeException {

    private final String code;
    private final Map<String, String> details;
    private final transient List<EndpointCandidate> candidates;

    public EndpointSpecException(String code, String message) {
        this(code, message, Map.of(), List.of());
    }

    public EndpointSpecException(String code, String message, Map<String, String> details) {
        this(code, message, details, List.of());
    }

    public EndpointSpecException(
        String code,
        String message,
        Map<String, String> details,
        List<EndpointCandidate> candidates
    ) {
        super(message);
        this.code = code;
        this.details = details;
        this.candidates = candidates;
    }
}
