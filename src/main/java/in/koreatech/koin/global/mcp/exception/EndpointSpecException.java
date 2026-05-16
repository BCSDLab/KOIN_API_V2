package in.koreatech.koin.global.mcp.exception;

public class EndpointSpecException extends RuntimeException {

    private final String code;

    public EndpointSpecException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
