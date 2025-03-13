package in.koreatech.koin._common.exception;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Getter;

@Getter
public class ErrorResponse {

    @JsonIgnore
    private final int status;
    private final int code;
    private final String message;
    private final String errorTraceId;

    public ErrorResponse(int status, String message, String errorTraceId) {
        this.status = status;
        this.code = 0;
        this.message = message;
        this.errorTraceId = errorTraceId;
    }
}
