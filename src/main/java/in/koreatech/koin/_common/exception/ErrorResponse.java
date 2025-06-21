package in.koreatech.koin._common.exception;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Getter;

@Getter
public class ErrorResponse {

    @JsonIgnore
    private final int status;
    private final String code;
    private final String message;
    private final String errorTraceId;

    public ErrorResponse(int status, String message, String errorTraceId) {
        this.status = status;
        this.code = "";
        this.message = message;
        this.errorTraceId = errorTraceId;
    }

    public ErrorResponse(int status, String code, String message, String errorTraceId) {
        this.status = status;
        this.code = code;
        this.message = message;
        this.errorTraceId = errorTraceId;
    }
}
