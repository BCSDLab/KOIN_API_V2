package in.koreatech.koin._common.exception;

import javax.annotation.Nonnull;

import org.springframework.http.HttpStatus;

import in.koreatech.koin._common.exception.errorcode.ErrorCode;
import io.micrometer.common.util.StringUtils;
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

    public static CustomException of(ErrorCode errorCode) {
        return CustomException.builder()
            .errorCode(errorCode)
            .detail("")
            .build();
    }

    public static CustomException of(ErrorCode errorCode, String detail) {
        return CustomException.builder()
            .errorCode(errorCode)
            .detail(detail)
            .build();
    }

    public static CustomException of(ErrorCode errorCode, @Nonnull Object errorObject) {
        return CustomException.builder()
            .errorCode(errorCode)
            .detail(errorObject.toString())
            .build();
    }

    public String getErrorCode() {
        return errorCode.name();
    }

    public HttpStatus getHttpStatus() {
        return errorCode.getHttpStatus();
    }

    public String getFullMessage() {
        if (StringUtils.isBlank(detail)) {
            return getMessage();
        }
        return String.format("%s: %s", getMessage(), detail);
    }
}
