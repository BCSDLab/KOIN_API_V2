package in.koreatech.koin.global.exception;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Getter;

@Getter
public class ErrorResponse {

    @JsonIgnore
    private final int status;
    private final String message;
    private final String code;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private final LocalDateTime time;

    public ErrorResponse(int status, String message) {
        this.status = status;
        this.message = message;
        this.code = "";
        this.time = LocalDateTime.now();
    }
}
