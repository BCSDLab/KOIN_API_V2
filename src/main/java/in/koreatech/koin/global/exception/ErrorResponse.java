package in.koreatech.koin.global.exception;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Getter;

@Getter
public class ErrorResponse {

    @JsonIgnore
    private final int status;
    private final String message;
    private final String code;
    private final String errorTraceId;

    public ErrorResponse(int status, String message, String errorTraceId) {
        this.status = status;
        this.message = message;
        this.code = "";
        this.errorTraceId = errorTraceId;
    }
}
