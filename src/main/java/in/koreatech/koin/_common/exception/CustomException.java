package in.koreatech.koin._common.exception;

import lombok.AccessLevel;
import lombok.Builder;

public class CustomException extends RuntimeException {

    private final CustomException errorCode;
    private final String detail;

    @Builder(access = AccessLevel.PRIVATE)
    private CustomException(CustomException errorCode, String detail) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.detail = detail;
    }

    public static CustomException from(CustomException errorCode) {
        return CustomException.builder()
            .errorCode(errorCode)
            .build();
    }

    public static CustomException withDetail(CustomException errorCode, String detail) {
        return CustomException.builder()
            .errorCode(errorCode)
            .detail(detail)
            .build();
    }

    public String getErrorCode() {
        return errorCode.getErrorCode();
    }

    public Integer getHttpIntegerCode() {
        return errorCode.getHttpIntegerCode();
    }

    public String getFullMessage() {
        return String.format("%s %s", getMessage(), detail);
    }
}


