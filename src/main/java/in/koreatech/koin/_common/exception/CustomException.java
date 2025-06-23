package in.koreatech.koin._common.exception;

import org.springframework.http.HttpStatus;

import lombok.AccessLevel;
import lombok.Builder;

public class CustomException extends RuntimeException {

    private final ErrorCode errorCode;
    private final String detail;

    @Builder(access = AccessLevel.PRIVATE)
    private CustomException(ErrorCode errorCode, String detail) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.detail = detail;
    }

    public static CustomException from(ErrorCode errorCode) {
        return CustomException.builder()
            .errorCode(errorCode)
            .build();
    }

    public static CustomException withDetail(ErrorCode errorCode, String detail) {
        return CustomException.builder()
            .errorCode(errorCode)
            .detail(detail)
            .build();
    }

    public String getErrorCode() {
        return errorCode.getErrorCode();
    }

    public HttpStatus getHttpStatus() {
        return errorCode.getHttpStatus();
    }

    public String getFullMessage() {
        return String.format("%s %s", getMessage(), detail);
    }
}
