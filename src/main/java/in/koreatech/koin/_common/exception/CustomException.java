package in.koreatech.koin._common.exception;

import java.util.Objects;

import org.springframework.http.HttpStatus;

import in.koreatech.koin._common.exception.errorcode.ErrorCode;
import io.micrometer.common.util.StringUtils;
import lombok.Getter;

public class CustomException extends RuntimeException {

    @Getter
    private final ErrorCode errorCode;
    private final String detail;

    private CustomException(ErrorCode errorCode, String detail) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.detail = detail;
    }

    public static CustomException of(ErrorCode errorCode) {
        return new CustomException(errorCode, "");
    }

    public static CustomException of(ErrorCode errorCode, Object errorObject) {
        return new CustomException(errorCode, Objects.toString(errorObject, ""));
    }

    public String getFullMessage() {
        if (StringUtils.isBlank(detail)) {
            return getMessage();
        }
        return String.format("%s: %s", getMessage(), detail);
    }
}
