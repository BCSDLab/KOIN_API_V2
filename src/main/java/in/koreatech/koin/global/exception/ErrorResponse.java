package in.koreatech.koin.global.exception;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Getter;

@Getter
public class ErrorResponse {

    @JsonIgnore
    private final int status;
    private final String message;
    private final String code;

    public ErrorResponse(int status, String message) {
        this.status = status;
        this.message = message;
        this.code = "";
    }
}
