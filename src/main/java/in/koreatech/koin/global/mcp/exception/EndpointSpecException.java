package in.koreatech.koin.global.mcp.exception;

import lombok.Getter;

@Getter
public class EndpointSpecException extends RuntimeException {

    private final String code;

    public EndpointSpecException(String code, String message) {
        super(message);
        this.code = code;
    }
}
