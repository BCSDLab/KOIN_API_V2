package in.koreatech.koin._common.exception;

import java.util.Objects;

import org.springframework.util.StringUtils;

import in.koreatech.koin._common.code.ApiResponseCode;
import lombok.Getter;

public class CustomException extends RuntimeException {

    @Getter
    private final ApiResponseCode errorCode;
    private final String detail;

    private CustomException(ApiResponseCode errorCode, String detail) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.detail = detail;
    }

    public static CustomException of(ApiResponseCode errorCode) {
        return new CustomException(errorCode, "");
    }

    public static CustomException of(ApiResponseCode errorCode, Object errorObject) {
        return new CustomException(errorCode, Objects.toString(errorObject, ""));
    }

    public String getFullMessage() {
        if (StringUtils.hasText(detail)) {
            return super.getMessage();
        }
        return String.format("%s: %s", getMessage(), detail);
    }
}
